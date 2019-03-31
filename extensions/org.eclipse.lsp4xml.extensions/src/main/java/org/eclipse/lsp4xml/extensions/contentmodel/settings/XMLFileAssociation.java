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
package org.eclipse.lsp4xml.extensions.contentmodel.settings;

import org.eclipse.lsp4xml.uriresolver.IExternalSchemaLocationProvider;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * XML file association between a XML file pattern (glob) and an XML Schema file
 * (systemId).
 **/
public class XMLFileAssociation {

	private transient PathMatcher pathMatcher;
	private transient Map<String, String> externalSchemaLocation;
	private String pattern;
	private String systemId;

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		this.pathMatcher = null;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
		this.externalSchemaLocation = null;
	}

	public boolean matches(String uri) {
		try {
			return matches(new URI(uri));
		} catch (Exception e) {
			return false;
		}
	}

	public boolean matches(URI uri) {
		if (pattern.length() < 1) {
			return false;
		}
		if (pathMatcher == null) {
			char c = pattern.charAt(0);
			String glob = pattern;
			if (c != '*' && c != '?' && c != '/') {
				// in case of pattern like this pattern="myFile*.xml", we must add '**/' before
				glob = "**/" + glob;
			}
			pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
		}
		try {
			return pathMatcher.matches(Paths.get(uri));
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return false;
	}

	public Map<String, String> getExternalSchemaLocation() {
		if (externalSchemaLocation == null) {
			this.externalSchemaLocation = new HashMap<String, String>();
			this.externalSchemaLocation.put(IExternalSchemaLocationProvider.NO_NAMESPACE_SCHEMA_LOCATION, systemId);
		}
		return externalSchemaLocation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		result = prime * result + ((systemId == null) ? 0 : systemId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XMLFileAssociation other = (XMLFileAssociation) obj;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		if (systemId == null) {
			if (other.systemId != null)
				return false;
		} else if (!systemId.equals(other.systemId))
			return false;
		return true;
	}
}
