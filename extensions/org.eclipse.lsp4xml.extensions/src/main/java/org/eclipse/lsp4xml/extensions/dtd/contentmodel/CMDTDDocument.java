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
package org.eclipse.lsp4xml.extensions.dtd.contentmodel;

import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * DTD document.
 * 
 * @author azerr
 *
 */
public class CMDTDDocument extends XMLDTDLoader implements CMDocument {

	private Map<String, List<String>> hierachiesMap;
	private List<CMElementDeclaration> elements;
	private DTDGrammar grammar;
	private List<String> hierachies;

	@Override
	public Collection<CMElementDeclaration> getElements() {
		if (elements == null) {
			elements = new ArrayList<>();
			int index = grammar.getFirstElementDeclIndex();
			while (index != -1) {
				CMDTDElementDeclaration elementDecl = new CMDTDElementDeclaration(this, index);
				grammar.getElementDecl(index, elementDecl);
				elements.add(elementDecl);
				index = grammar.getNextElementDeclIndex(index);
			}

		}
		return elements;
	}

	@Override
	public CMElementDeclaration findCMElement(DOMElement element, String namespace) {
		List<DOMElement> paths = new ArrayList<>();
		while (element != null && (namespace == null || namespace.equals(element.getNamespaceURI()))) {
			paths.add(0, element);
			element = element.getParentNode() instanceof DOMElement ? (DOMElement) element.getParentNode() : null;
		}
		CMElementDeclaration declaration = null;
		for (int i = 0; i < paths.size(); i++) {
			DOMElement elt = paths.get(i);
			if (i == 0) {
				declaration = findElementDeclaration(elt.getLocalName(), namespace);
			} else {
				declaration = declaration.findCMElement(elt.getLocalName(), namespace);
			}
			if (declaration == null) {
				break;
			}
		}
		return declaration;
	}

	private CMElementDeclaration findElementDeclaration(String tag, String namespace) {
		for (CMElementDeclaration cmElement : getElements()) {
			if (cmElement.getName().equals(tag)) {
				return cmElement;
			}
		}
		return null;
	}

	@Override
	public void startContentModel(String elementName, Augmentations augs) throws XNIException {
		if (hierachiesMap == null) {
			hierachiesMap = new HashMap<>();
		}
		hierachies = new ArrayList<>();
		hierachiesMap.put(elementName, hierachies);
		super.startContentModel(elementName, augs);
	}

	@Override
	public void element(String elementName, Augmentations augs) throws XNIException {
		hierachies.add(elementName);
		super.element(elementName, augs);
	}

	@Override
	public void endContentModel(Augmentations augs) throws XNIException {
		hierachies = null;
		super.endContentModel(augs);
	}

	@Override
	public Grammar loadGrammar(XMLInputSource source) throws IOException, XNIException {
		grammar = (DTDGrammar) super.loadGrammar(source);
		return grammar;
	}

	public void loadInternalDTD(String internalSubset, String baseSystemId, String systemId)
			throws XNIException, IOException {
		// Load empty DTD grammar
		XMLInputSource source = new XMLInputSource("", "", "", new StringReader(""), "");
		grammar = (DTDGrammar) loadGrammar(source);
		// To get the DTD scanner to end at the right place we have to fool
		// it into thinking that it reached the end of the internal subset
		// in a real document.
		fDTDScanner.reset();
		StringBuilder buffer = new StringBuilder(internalSubset.length() + 2);
		buffer.append(internalSubset).append("]>");
		XMLInputSource is = new XMLInputSource(null, baseSystemId, null, new StringReader(buffer.toString()), null);
		fEntityManager.startDocumentEntity(is);
		fDTDScanner.scanDTDInternalSubset(true, false, systemId != null);
	}

	void collectElementsDeclaration(String elementName, List<CMElementDeclaration> elements) {
		if (hierachiesMap == null) {
			return;
		}
		List<String> children = hierachiesMap.get(elementName);
		if (children == null) {
			return;
		}
		children.stream().forEach(name -> {
			CMElementDeclaration element = findElementDeclaration(name, null);
			if (element != null) {
				elements.add(element);
			}
		});
	}

	void collectAttributesDeclaration(CMDTDElementDeclaration elementDecl, List<CMAttributeDeclaration> attributes) {
		int elementDeclIndex = grammar.getElementDeclIndex(elementDecl.name);
		int index = grammar.getFirstAttributeDeclIndex(elementDeclIndex);
		while (index != -1) {
			CMDTDAttributeDeclaration attributeDecl = new CMDTDAttributeDeclaration();
			grammar.getAttributeDecl(index, attributeDecl);
			attributes.add(attributeDecl);
			index = grammar.getNextAttributeDeclIndex(index);
		}
	}
}
