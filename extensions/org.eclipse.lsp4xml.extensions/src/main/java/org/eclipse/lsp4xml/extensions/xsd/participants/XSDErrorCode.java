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
package org.eclipse.lsp4xml.extensions.xsd.participants;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.services.extensions.diagnostics.IXMLErrorCode;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

import java.util.HashMap;
import java.util.Map;

/**
 * XSD error code.
 * 
 * @see https://wiki.xmldation.com/Support/Validator
 *
 */
public enum XSDErrorCode implements IXMLErrorCode {

	s4s_elt_invalid_content_1("s4s-elt-invalid-content.1"), //
	s4s_elt_must_match_1("s4s-elt-must-match.1"), //
	s4s_att_must_appear("s4s-att-must-appear"), //
	s4s_elt_invalid_content_2("s4s-elt-invalid-content.2"), //
	s4s_att_not_allowed("s4s-att-not-allowed"), //
	s4s_att_invalid_value("s4s-att-invalid-value"), //
	s4s_elt_character("s4s-elt-character"), //
	src_resolve_4_2("src-resolve.4.2"), //
	src_resolve("src-resolve");

	private final String code;

	private XSDErrorCode() {
		this(null);
	}

	private XSDErrorCode(String code) {
		this.code = code;
	}

	@Override
	public String getCode() {
		if (code == null) {
			return name();
		}
		return code;
	}

	private final static Map<String, XSDErrorCode> codes;

	static {
		codes = new HashMap<>();
		for (XSDErrorCode errorCode : values()) {
			codes.put(errorCode.getCode(), errorCode);
		}
	}

	public static XSDErrorCode get(String name) {
		return codes.get(name);
	}

	/**
	 * Create the LSP range from the SAX error.
	 * 
	 * @param characterOffset
	 * @param key
	 * @param arguments
	 * @param document
	 * @return the LSP range from the SAX error.
	 */
	public static Range toLSPRange(XMLLocator location, XSDErrorCode code, Object[] arguments, DOMDocument document) {
		int offset = location.getCharacterOffset() - 1;
		// adjust positions
		switch (code) {
		case s4s_elt_invalid_content_1:
		case s4s_elt_must_match_1:
		case s4s_att_must_appear:
		case s4s_elt_invalid_content_2:
			return XMLPositionUtility.selectStartTag(offset, document);
		case s4s_att_not_allowed:
			return XMLPositionUtility.selectAttributeNameAt(offset, document);
		case s4s_att_invalid_value: {
			String attrName = "";
			return XMLPositionUtility.selectAttributeValueAt(attrName, offset, document);
		}
		case s4s_elt_character:
			return XMLPositionUtility.selectText(offset, document);
		case src_resolve_4_2:
		case src_resolve:
			String attrValue = (String) arguments[2];
			return XMLPositionUtility.selectAttributeValueByGivenValueAt(attrValue, offset, document);
		}
		return null;
	}
}
