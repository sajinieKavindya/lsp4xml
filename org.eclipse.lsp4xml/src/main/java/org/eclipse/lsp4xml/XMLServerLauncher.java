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
package org.eclipse.lsp4xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4xml.commons.ParentProcessWatcher;

public class XMLServerLauncher {

	/**
	 * Calls {@link #launch(InputStream, OutputStream)}, using the standard input
	 * and output streams.
	 */
	public static void main(String[] args) {
		final String username = System.getProperty("http.proxyUser");
		final String password = System.getProperty("http.proxyPassword");
		if (username != null && password != null) {
			Authenticator.setDefault(new Authenticator() {

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}

			});
		}
		launch(System.in, System.out);
	}

	/**
	 * Launches {@link XMLLanguageServer} and makes it accessible through the JSON
	 * RPC protocol defined by the LSP.
	 * 
	 * @param launcherFuture The future returned by
	 *                       {@link org.eclipse.lsp4j.jsonrpc.Launcher#startListening()}.
	 *                       (I'm not 100% sure how it meant to be used though, as
	 *                       it's undocumented...)
	 */
	public static Future<?> launch(InputStream in, OutputStream out) {
		XMLLanguageServer server = new XMLLanguageServer();
		Function<MessageConsumer, MessageConsumer> wrapper;
		if ("false".equals(System.getProperty("watchParentProcess"))) {
			wrapper = it -> it;
		} else {
			wrapper = new ParentProcessWatcher(server);
		}
		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out,
				Executors.newCachedThreadPool(), wrapper);
		server.setClient(launcher.getRemoteProxy());
		return launcher.startListening();
	}
}
