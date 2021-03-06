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
package org.eclipse.lsp4xml.extensions.emmet.emmetio.expand;

import org.junit.Assert;
import org.junit.Test;

public class ExpandAbbreviationTest {

	@Test
	public void testExpand() {
		ExpandOptions options = new ExpandOptions();
		options.syntax = "xsl";
		String code = ExpandAbbreviation.expand("a>b", options);
		Assert.assertEquals("<a><b></b></a>", code);
	}
}
