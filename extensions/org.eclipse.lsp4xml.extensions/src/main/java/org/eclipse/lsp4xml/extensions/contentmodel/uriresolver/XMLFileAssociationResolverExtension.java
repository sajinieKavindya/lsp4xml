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
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLFileAssociation;
import org.eclipse.lsp4xml.uriresolver.IExternalSchemaLocationProvider;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtension;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

/**
 * XML file association URI resolver.
 *
 */
public class XMLFileAssociationResolverExtension implements URIResolverExtension, IExternalSchemaLocationProvider {

	private String rootUri;

	private XMLFileAssociation[] fileAssociations;

	/**
	 * 
	 * @param fileAssociations
	 * @return true if file associations changed and false otherwise
	 */
	public boolean setFileAssociations(XMLFileAssociation[] fileAssociations) {
		XMLFileAssociation[] oldFileAssociations = this.fileAssociations;
		this.fileAssociations = fileAssociations;
		expandSystemId();
		return !Arrays.equals(oldFileAssociations, fileAssociations);
	}

	@Override
	public String resolve(String baseLocation, String publicId, String systemId) {
		if (systemId != null) {
			// system id is defined in the XML root element (ex : systemId=Types.xsd for
			// <Types
			// xsi:noNamespaceSchemaLocation="Types.xsd">
			// ignore XML file association
			return null;
		}
		if (fileAssociations != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				if (fileAssociation.matches(baseLocation)) {
					return fileAssociation.getSystemId();
				}
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getExternalSchemaLocation(URI fileURI) {
		if (fileAssociations != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				if (fileAssociation.matches(fileURI)) {
					return fileAssociation.getExternalSchemaLocation();
				}
			}
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

	private void expandSystemId() {
		if (fileAssociations != null && rootUri != null) {
			for (XMLFileAssociation fileAssociation : fileAssociations) {
				// Expand original system id by using the root uri.
				try {
					String expandSystemId = XMLEntityManager.expandSystemId(fileAssociation.getSystemId(), rootUri,
							false);
					if (expandSystemId != null) {
						fileAssociation.setSystemId(expandSystemId);
					}
				} catch (MalformedURIException e) {
					// Do nothing
				}
			}
		}

	}
}
