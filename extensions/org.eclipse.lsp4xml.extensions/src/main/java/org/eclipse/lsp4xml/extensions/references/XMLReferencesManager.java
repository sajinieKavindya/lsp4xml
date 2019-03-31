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
package org.eclipse.lsp4xml.extensions.references;

import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class XMLReferencesManager {

	private static final XMLReferencesManager INSTANCE = new XMLReferencesManager();

	public static XMLReferencesManager getInstance() {
		return INSTANCE;
	}

	private final List<XMLReferences> referencesCache;

	public XMLReferencesManager() {
		this.referencesCache = new ArrayList<>();
	}

	public XMLReferences referencesFor(Predicate<DOMDocument> documentPredicate) {
		XMLReferences references = new XMLReferences(documentPredicate);
		referencesCache.add(references);
		return references;
	}

	public void collect(DOMNode node, Consumer<DOMNode> collector) {
		DOMDocument document = node.getOwnerDocument();

		for (XMLReferences references : referencesCache) {
			if (references.canApply(document)) {
				try {
					references.collectNodes(node, collector);
				} catch (XPathExpressionException e) {
					// TODO!!!
					e.printStackTrace();
				}
			}
		}
	}

}
