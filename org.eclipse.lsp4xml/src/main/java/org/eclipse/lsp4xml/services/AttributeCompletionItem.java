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
package org.eclipse.lsp4xml.services;

import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.settings.SharedSettings;

public class AttributeCompletionItem extends CompletionItem {

	/**
	 * Attribute completion item.
	 * 
	 * @param attrName           attribute name
	 * @param canSupportSnippets true if snippets is supported to generate attribute
	 *                           value and false otherwise
	 * @param fullRange          the range to edit.
	 * @param generateValue      true if attribute value must be generated and false
	 *                           otherwise.
	 * @param defaultValue       the default value of attribute.
	 * @param enumerationValues  the enumeration values of attribute.
	 */
	public AttributeCompletionItem(String attrName, boolean canSupportSnippets, Range fullRange, boolean generateValue,
			String defaultValue, Collection<String> enumerationValues, SharedSettings settings) {
		super.setLabel(attrName);
		super.setKind(CompletionItemKind.Unit);
		super.setFilterText(attrName);
		StringBuilder attributeContent = new StringBuilder(attrName);
		if (generateValue) {
			// Generate attribute value content
			String attributeValue = XMLGenerator.generateAttributeValue(defaultValue, enumerationValues,
					canSupportSnippets, 1, true, settings);
			attributeContent.append(attributeValue);
		}
		super.setTextEdit(new TextEdit(fullRange, attributeContent.toString()));
		super.setInsertTextFormat(canSupportSnippets ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);
	}
}
