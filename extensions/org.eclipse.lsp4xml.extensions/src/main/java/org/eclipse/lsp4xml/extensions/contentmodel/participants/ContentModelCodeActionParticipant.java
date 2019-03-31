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
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension to support XML code actions based on content model (XML Schema
 * completion, etc)
 */
public class ContentModelCodeActionParticipant implements ICodeActionParticipant {

	private final Map<String, ICodeActionParticipant> codeActionParticipants;

	public ContentModelCodeActionParticipant() {
		codeActionParticipants = new HashMap<>();
		XMLSyntaxErrorCode.registerCodeActionParticipants(codeActionParticipants);
		DTDErrorCode.registerCodeActionParticipants(codeActionParticipants);
		XMLSchemaErrorCode.registerCodeActionParticipants(codeActionParticipants);
	}

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		ICodeActionParticipant participant = codeActionParticipants.get(diagnostic.getCode());
		if (participant != null) {
			participant.doCodeAction(diagnostic, range, document, codeActions, formattingSettings, componentProvider);
		}
	}
}
