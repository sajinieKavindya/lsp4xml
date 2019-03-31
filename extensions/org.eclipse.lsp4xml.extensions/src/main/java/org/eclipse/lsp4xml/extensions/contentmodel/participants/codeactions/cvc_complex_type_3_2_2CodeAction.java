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
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

import java.util.List;

/**
 * Code action to fix cvc-complex-type.3.2.2 error.
 *
 */
public class cvc_complex_type_3_2_2CodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		Range diagnosticRange = diagnostic.getRange();
		try {
			int offset = document.offsetAt(diagnosticRange.getEnd());
			DOMAttr attr = document.findAttrAt(offset);
			if (attr != null) {
				// Remove attribute
				int startOffset = attr.getStart();
				int endOffset = attr.getEnd();
				Range attrRange = new Range(document.positionAt(startOffset), document.positionAt(endOffset));
				CodeAction removeAttributeAction = CodeActionFactory.remove("Remove '" + attr.getName() + "' attribute",
						attrRange, document.getTextDocument(), diagnostic);
				codeActions.add(removeAttributeAction);
			}
		} catch (BadLocationException e) {
			// Do nothing
		}
	}

}
