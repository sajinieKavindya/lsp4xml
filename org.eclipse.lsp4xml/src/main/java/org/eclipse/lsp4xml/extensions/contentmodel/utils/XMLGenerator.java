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
package org.eclipse.lsp4xml.extensions.contentmodel.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.lsp4xml.commons.SnippetsBuilder;
import org.eclipse.lsp4xml.extensions.contentmodel.completions.SnippetProvider;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.utils.XMLBuilder;

/**
 * XML generator used to generate an XML fragment with formatting from a given
 * element declaration (XML Schema element declaration, DTD element, etc).
 */
public class XMLGenerator {

	private final XMLFormattingOptions formattingOptions;
	private final String whitespacesIndent;
	private final String lineDelimiter;
	private final boolean canSupportSnippets;
	private final boolean autoCloseTags;
	private int maxLevel;

	/**
	 * XML generator constructor.
	 * 
	 * @param formattingOptions  the formatting options (uses spaces or tabs for
	 *                           indentation, etc)
	 * @param whitespacesIndent  the whitespaces to use to indent XML children
	 *                           elements.
	 * @param lineDelimiter      the line delimiter to use when several XML elements
	 *                           must be generated.
	 * @param canSupportSnippets true if snippets can be supported and false
	 *                           otherwise.
	 */
	public XMLGenerator(XMLFormattingOptions formattingOptions, String whitespacesIndent, String lineDelimiter,
			boolean canSupportSnippets, int maxLevel) {
		this(formattingOptions, true, whitespacesIndent, lineDelimiter, canSupportSnippets, maxLevel);
	}

	public XMLGenerator(XMLFormattingOptions formattingOptions, boolean autoCloseTags, String whitespacesIndent,
			String lineDelimiter, boolean canSupportSnippets, int maxLevel) {
		this.formattingOptions = formattingOptions;
		this.autoCloseTags = autoCloseTags;
		this.whitespacesIndent = whitespacesIndent;
		this.lineDelimiter = lineDelimiter;
		this.canSupportSnippets = canSupportSnippets;
	}

//	public String generate(CMElementDeclaration elementDeclaration) {
//		return generate(elementDeclaration, null);
//	}

	/**
	 * Returns the XML generated from the given element declaration.
	 * 
	 * @param elementDeclaration
	 * @param prefix
	 * @return the XML generated from the given element declaration.
	 */
	public String generate(CMElementDeclaration elementDeclaration, String prefix) {
		XMLBuilder xml = new XMLBuilder(formattingOptions, whitespacesIndent, lineDelimiter);
		generate(elementDeclaration, prefix, 0, 0, xml, new ArrayList<CMElementDeclaration>());
		if (canSupportSnippets) {
			xml.addContent(SnippetsBuilder.tabstops(0)); // "$0"
		}
		return xml.toString();
	}

	public String generateSnippet(CMElementDeclaration elementDeclaration) {

		String keyword = elementDeclaration.getName();
		return SnippetProvider.snippets.get(keyword);

	}

	public String generateSnippetForProLog() {
		return SnippetProvider.snippets.get("xml");
	}

	private int generate(CMElementDeclaration elementDeclaration, String prefix, int level, int snippetIndex,
			XMLBuilder xml, List<CMElementDeclaration> generatedElements) {
		if (generatedElements.contains(elementDeclaration)) {
			return snippetIndex;
		}
		generatedElements.add(elementDeclaration);
		if (level > 0) {
			xml.linefeed();
			xml.indent(level);
		}

		xml.startElement(prefix, elementDeclaration.getName(), false);
		// Attributes
		Collection<CMAttributeDeclaration> attributes = elementDeclaration.getAttributes();
		snippetIndex = generate(attributes, level, snippetIndex, xml, elementDeclaration.getName());
		// Elements children
		Collection<CMElementDeclaration> children = elementDeclaration.getElements();

		if (children.size() > 0) {
			xml.closeStartElement();


			if ((level > maxLevel)) {
				level++;
				for (CMElementDeclaration child : children) {
					snippetIndex = generate(child, prefix, level, snippetIndex, xml, generatedElements);
				}
				level--;
				xml.linefeed();
				xml.indent(level);
			} else {
				if (canSupportSnippets) {
					snippetIndex++;
					xml.addContent(SnippetsBuilder.tabstops(snippetIndex));
				}
			}
			if (autoCloseTags) {
				xml.endElement(prefix, elementDeclaration.getName());
			}
		} else if (elementDeclaration.isEmpty() && autoCloseTags) {
			xml.selfCloseElement();
		} else {
			xml.closeStartElement();
			if (canSupportSnippets) {
				snippetIndex++;
				xml.addContent(SnippetsBuilder.tabstops(snippetIndex));
			}
			if (autoCloseTags) {
				xml.endElement(prefix, elementDeclaration.getName());
			}
		}
		return snippetIndex;
	}

	public String generate(Collection<CMAttributeDeclaration> attributes, String tagName) {
		XMLBuilder xml = new XMLBuilder(formattingOptions, whitespacesIndent, lineDelimiter);
		generate(attributes, 0, 0, xml, tagName);
		return xml.toString();
	}

	private int generate(Collection<CMAttributeDeclaration> attributes, int level, int snippetIndex, XMLBuilder xml,
			String tagName) {
		int attributeIndex = 0;
		List<CMAttributeDeclaration> requiredAttributes = new ArrayList<CMAttributeDeclaration>();
		for (CMAttributeDeclaration att : attributes) {
			if (att.isRequired()) {
				requiredAttributes.add(att);
			}
		}
		int attributesSize = requiredAttributes.size();
		for (CMAttributeDeclaration attributeDeclaration : requiredAttributes) {
			if (canSupportSnippets) {
				snippetIndex++;
			}
			String defaultValue = attributeDeclaration.getDefaultValue();
			Collection<String> enumerationValues = attributeDeclaration.getEnumerationValues();
			String value = generateAttributeValue(defaultValue, enumerationValues, canSupportSnippets, snippetIndex,
					false);
			if (attributesSize != 1) {
				xml.addAttribute(attributeDeclaration.getName(), value, attributeIndex, level, tagName);
			} else {
				xml.addSingleAttribute(attributeDeclaration.getName(), value);
			}
			attributeIndex++;
		}
		return snippetIndex;
	}

	public static String generateAttributeValue(String defaultValue, Collection<String> enumerationValues,
			boolean canSupportSnippets, int snippetIndex, boolean withQuote) {
		StringBuilder value = new StringBuilder();
		if (withQuote) {
			value.append("=\"");
		}
		if (!canSupportSnippets) {
			if (defaultValue != null) {
				value.append(defaultValue);
			}
		} else {
			// Snippets syntax support
			if (enumerationValues != null && !enumerationValues.isEmpty()) {
				SnippetsBuilder.choice(snippetIndex, enumerationValues, value);
			} else {
				if (defaultValue != null) {
					SnippetsBuilder.placeholders(snippetIndex, defaultValue, value);
				} else {
					SnippetsBuilder.tabstops(snippetIndex, value);
				}
			}
		}
		if (withQuote) {
			value.append("\"");
			if (canSupportSnippets) {
				SnippetsBuilder.tabstops(0, value); // "$0"
			}
		}
		return value.toString();
	}

	private XSParticleDecl[] getXsParticleDecls(CMElementDeclaration cmElement) {
		XSElementDeclaration elementDeclaration = cmElement.getElementDeclaration();
		XSParticleDecl[] fParticles = null;
		XSTypeDefinition definition = null;
		XSParticle particle = null;
		XSTerm fValue = null;
		if (elementDeclaration instanceof XSElementDecl) {
			XSElementDecl xsElementDecl = (XSElementDecl) elementDeclaration;
			definition = xsElementDecl.fType;
		}if (definition != null && definition instanceof XSComplexTypeDecl) {
			XSComplexTypeDecl typeDecl = (XSComplexTypeDecl) definition;
			particle = typeDecl.getParticle();
		}if (particle != null && particle instanceof XSParticleDecl) {
			XSParticleDecl particleDecl = (XSParticleDecl) particle;
			fValue = particleDecl.fValue;
		}if (fValue instanceof XSModelGroupImpl) {
			XSModelGroupImpl modelGroup = (XSModelGroupImpl) fValue;
			fParticles = modelGroup.fParticles;

		}
		return fParticles;
	}

	private Collection<CMElementDeclaration> filterRequiredCmElements(Collection<CMElementDeclaration> cmElements,
																	  XSParticleDecl[] fParticles){

		Collection<CMElementDeclaration> resultantCMElements = new ArrayList<>();

		int index = 0;
		for (CMElementDeclaration child : cmElements) {
			int minOccurs = fParticles[index].fMinOccurs;
			if (minOccurs > 0) {
				((ArrayList<CMElementDeclaration>) resultantCMElements).add(child);
			}
			index++;
		}
		return resultantCMElements;

	}

}
