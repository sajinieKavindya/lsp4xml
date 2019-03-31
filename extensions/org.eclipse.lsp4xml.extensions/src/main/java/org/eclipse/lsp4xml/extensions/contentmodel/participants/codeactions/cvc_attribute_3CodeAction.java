/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

import java.util.List;

/**
 * Code action to fix cvc-attribute-3 error.
 *
 */
public class cvc_attribute_3CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		try {
			Range diagnosticRange = diagnostic.getRange();
			int offset = document.offsetAt(range.getStart());
			DOMAttr attr = document.findAttrAt(offset);
			if (attr != null) {
				String attributeName = attr.getName();
				ContentModelManager contentModelManager = componentProvider.getComponent(ContentModelManager.class);
				CMElementDeclaration cmElement = contentModelManager.findCMElement(attr.getOwnerElement());
				if (cmElement != null) {
					CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
					if (cmAttribute != null) {
						Range rangeValue = new Range(
								new Position(diagnosticRange.getStart().getLine(),
										diagnosticRange.getStart().getCharacter() + 1),
								new Position(diagnosticRange.getEnd().getLine(),
										diagnosticRange.getEnd().getCharacter() - 1));
						cmAttribute.getEnumerationValues().forEach(value -> {
							// Replace attribute value
							// value = "${1:" + value + "}";
							CodeAction replaceAttrValueAction = CodeActionFactory.replace(
									"Replace with '" + value + "'", rangeValue, value, document.getTextDocument(),
									diagnostic);
							codeActions.add(replaceAttrValueAction);
						});
					}
				}
			}
		} catch (Exception e) {
			// do nothing
		}
	}

}
