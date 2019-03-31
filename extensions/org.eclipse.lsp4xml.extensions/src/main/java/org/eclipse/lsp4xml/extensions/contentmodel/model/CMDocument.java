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
package org.eclipse.lsp4xml.extensions.contentmodel.model;

import org.eclipse.lsp4xml.dom.DOMElement;

import java.util.Collection;

/**
 * Content model document which abstracts element declaration from a given
 * grammar (XML Schema, DTD).
 */
public interface CMDocument {

	Collection<CMElementDeclaration> getElements();
	
	/**
	 * Returns the declared element which matches the given XML element and null
	 * otherwise.
	 * 
	 * @param element the XML element
	 * @return the declared element which matches the given XML element and null
	 *         otherwise.
	 */
	CMElementDeclaration findCMElement(DOMElement element, String namespace);

}
