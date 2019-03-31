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

import org.apache.xerces.xs.XSElementDeclaration;

import java.util.Collection;

/**
 * Content model element which abstracts element declaration from a given
 * grammar (XML Schema, DTD).
 */
public interface CMElementDeclaration {

	XSElementDeclaration getElementDeclaration();

	/**
	 * Returns the declared element name.
	 * 
	 * @return the declared element name.
	 */
	String getName();

	/**
	 * Returns the target namespace and null otherwise.
	 * 
	 * @return the target namespace and null otherwise.
	 */
	String getNamespace();

	/**
	 * Returns the declared element name with the given prefix.
	 * 
	 * @return the declared element name with the given prefix.
	 */
	default String getName(String prefix) {
		String name = getName();
		if (prefix == null || prefix.isEmpty()) {
			return name;
		}
		return prefix + ":" + name;
	}

	/**
	 * Returns the attributes of this declared element.
	 * 
	 * @return the attributes element of this declared element.
	 */
	Collection<CMAttributeDeclaration> getAttributes();

	/**
	 * Returns the children declared element of this declared element.
	 * 
	 * @return the children declared element of this declared element.
	 */
	Collection<CMElementDeclaration> getElements();

	/**
	 * Returns the declared element which matches the given XML tag name / namespace
	 * and null otherwise.
	 * 
	 * @param tag
	 * @param namespace
	 * @return the declared element which matches the given XML tag name / namespace
	 *         and null otherwise.
	 */
	CMElementDeclaration findCMElement(String tag, String namespace);

	/**
	 * Returns the declared attribute which match the given name and null otherwise.
	 * 
	 * @param attributeName
	 * @return the declared attribute which match the given name and null otherwise.
	 */
	CMAttributeDeclaration findCMAttribute(String attributeName);

	/**
	 * Returns the documentation of the declared element.
	 * 
	 * @return the documentation of the declared element.
	 */
	String getDocumentation();

	/**
	 * Returns true if the element cannot contains element children or text content
	 * and false otherwise.
	 * 
	 * @return true if the element cannot contains element children or text content
	 *         and false otherwise.
	 */
	boolean isEmpty();

	Collection<String> getEnumerationValues();

}
