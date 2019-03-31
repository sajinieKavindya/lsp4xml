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
package org.eclipse.lsp4xml.extensions.xsd.contentmodel;

import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSObjectList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;

import static org.eclipse.lsp4xml.utils.StringUtils.normalizeSpace;

/**
 * Extract xs:document & xs:appinfo from the xs:annotation.
 *
 */
class XSDAnnotationModel {

	String appInfo;

	String documentation;

	private XSDAnnotationModel() {

	}

	public String getAppInfo() {
		return appInfo;
	}

	public String getDocumentation() {
		return documentation;
	}

	public static String getDocumentation(XSObjectList annotations) {
		if (annotations == null) {
			return "";
		}
		StringBuilder doc = new StringBuilder();
		for (Object object : annotations) {
			XSAnnotation annotation = (XSAnnotation) object;
			XSDAnnotationModel annotationModel = XSDAnnotationModel.load(annotation);
			if (annotationModel != null) {
				if (annotationModel.getAppInfo() != null) {
					doc.append(annotationModel.getAppInfo());
				}
				if (annotationModel.getDocumentation() != null) {
					doc.append(annotationModel.getDocumentation());
				}
			}
		}
		return doc.toString();
	}

	public static XSDAnnotationModel load(XSAnnotation annotation) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XSAnnotationHandler handler = new XSAnnotationHandler();
			saxParser.parse(new InputSource(new StringReader(annotation.getAnnotationString())), handler);
			return handler.getModel();
		} catch (Exception e) {
			return null;
		}
	}

	private static class XSAnnotationHandler extends DefaultHandler {

		private static final String APPINFO_ELEMENT = "appinfo";
		private static final String DOCUMENTATION_ELEMENT = "documentation";

		private StringBuilder current;
		private final XSDAnnotationModel model;

		public XSAnnotationHandler() {
			model = new XSDAnnotationModel();
		}

		public XSDAnnotationModel getModel() {
			return model;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (qName.endsWith(DOCUMENTATION_ELEMENT) || qName.endsWith(APPINFO_ELEMENT)) {
				current = new StringBuilder();
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (current != null) {
				if (qName.endsWith(APPINFO_ELEMENT)) {
					model.appInfo = normalizeSpace(current.toString());
				} else if (qName.endsWith(DOCUMENTATION_ELEMENT)) {
					model.documentation = normalizeSpace(current.toString());
				}
				current = null;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (current != null) {
				current.append(ch, start, length);
			}
			super.characters(ch, start, length);
		}

	}

}
