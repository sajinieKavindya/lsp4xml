/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.uriresolver;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * XML catalog URI resolver.
 *
 */
public class XMLCatalogResolverExtension implements URIResolverExtension {

	private static final Logger LOGGER = Logger.getLogger(XMLCatalogResolverExtension.class.getName());

	private XMLCatalogResolver catalogResolver;
	private String rootUri;

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (catalogResolver != null) {
			try {

				// The namespace is useful for resolving namespace aware
				// grammars such as XML schema. Let it take precedence over
				// the external identifier if one exists.
				String namespace = publicId;
				if (namespace != null) {
					String resolvedId = catalogResolver.resolveURI(namespace);
					if (resolvedId != null) {
						return resolvedId;
					}
				}

				// Resolve against an external identifier if one exists. This
				// is useful for resolving DTD external subsets and other
				// external entities. For XML schemas if there was no namespace
				// mapping we might be able to resolve a system identifier
				// specified as a location hint.
				if (publicId != null && systemId != null) {
					return catalogResolver.resolvePublic(publicId, systemId);
				} else if (systemId != null) {
					return catalogResolver.resolveSystem(systemId);
				} else if (publicId != null) {
					return catalogResolver.resolvePublic(publicId, null);
				}
			} catch (Exception e) {

			}
		}
		return null;

	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		if (catalogResolver != null) {
			return catalogResolver.resolveEntity(resourceIdentifier);
		}
		return null;
	}

	/**
	 * Set the root URI
	 * 
	 * @param rootUri the root URI
	 */
	public void setRootUri(String rootUri) {
		this.rootUri = rootUri;
	}

	/**
	 * Initialize catalogs path.
	 * 
	 * @param catalogs the catalog path array.
	 * @return true if catalogs changed and false otherwise
	 */
	public boolean setCatalogs(String[] catalogs) {
		String[] oldCatalogs = catalogResolver != null ? catalogResolver.getCatalogList() : null;
		if (catalogs != null) {
			List<String> xmlCatalogFiles = new ArrayList<>();
			for (String catalogPath : catalogs) {
				// resolve catalog file path with root uri
				String fullPath = expandSystemId(catalogPath);
				// check if XML catalog path is valid
				boolean valid = XMLCatalogResolverExtension.isXMLCatalogFileValid(fullPath);
				if (valid) {
					xmlCatalogFiles.add(fullPath);
					LOGGER.info("Adding XML catalog '" + catalogPath + "' with expand system id '" + fullPath
							+ "' and root URI '" + rootUri + "'.");
				} else {
					LOGGER.severe("Cannot add XML catalog '" + catalogPath + "' with expand system id '" + fullPath
							+ "' and root URI '" + rootUri + "'.");
				}
			}
			if (xmlCatalogFiles.size() > 0) {
				XMLCatalogResolver catalogResolver = new XMLCatalogResolver(xmlCatalogFiles.toArray(new String[0]));
				setCatalogResolver(catalogResolver);
			} else {
				setCatalogResolver(null);
			}
		} else {
			setCatalogResolver(null);
		}
		String[] newCatalogs = catalogResolver != null ? catalogResolver.getCatalogList() : null;
		return !Objects.equals(oldCatalogs, newCatalogs);
	}

	private String expandSystemId(String path) {
		try {
			return XMLEntityManager.expandSystemId(path, rootUri, false);
		} catch (MalformedURIException e) {
			return path;
		}
	}

	/**
	 * Returns true if the XML catalog file exists and false otherwise.
	 * 
	 * @param catalogFile catalog file to check.
	 * @return true if the XML catalog file exists and false otherwise.
	 */
	private static boolean isXMLCatalogFileValid(String catalogFile) {
		try {
			return new File(new URI(catalogFile).getPath()).exists();
		} catch (URISyntaxException e) {
			return new File(catalogFile).exists();
		}
	}

	private void setCatalogResolver(XMLCatalogResolver catalogResolver) {
		this.catalogResolver = catalogResolver;
	}

	/**
	 * Refresh the XML catalogs.
	 */
	public void refreshCatalogs() {
		if (catalogResolver != null) {
			setCatalogs(catalogResolver.getCatalogList());
		}
	}
}
