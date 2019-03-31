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
package org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lsp4xml.services.extensions.diagnostics.AbstractLSPErrorReporter;

import java.util.List;

/**
 * LSP error reporter for XML syntax and error grammar (XML Schema/DTD).
 *
 */
public class LSPErrorReporterForXML extends AbstractLSPErrorReporter {

	private static final String XML_DIAGNOSTIC_SOURCE = "xml";

	public LSPErrorReporterForXML(DOMDocument xmlDocument, List<Diagnostic> diagnostics) {
		super(XML_DIAGNOSTIC_SOURCE, xmlDocument, diagnostics);
	}

	/**
	 * Create the LSP range from the SAX error.
	 * 
	 * @param location
	 * @param key
	 * @param arguments
	 * @param document
	 * @return the LSP range from the SAX error.
	 */
	@Override
	protected Range toLSPRange(XMLLocator location, String key, Object[] arguments, DOMDocument document) {
		// try adjust positions for XML syntax error
		XMLSyntaxErrorCode syntaxCode = XMLSyntaxErrorCode.get(key);
		if (syntaxCode != null) {
			Range range = XMLSyntaxErrorCode.toLSPRange(location, syntaxCode, arguments, document);
			if (range != null) {
				return range;
			}
		} else {
			// try adjust positions for XML schema error
			XMLSchemaErrorCode schemaCode = XMLSchemaErrorCode.get(key);
			if (schemaCode != null) {
				Range range = XMLSchemaErrorCode.toLSPRange(location, schemaCode, arguments, document);
				if (range != null) {
					return range;
				}
			} else {
				// try adjust positions for DTD error
				DTDErrorCode dtdCode = DTDErrorCode.get(key);
				if (dtdCode != null) {
					Range range = DTDErrorCode.toLSPRange(location, dtdCode, arguments, document);
					if (range != null) {
						return range;
					}
				}
			}
		}
		return null;
	}
}
