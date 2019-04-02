/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *  Red Hat Inc. - Dynamic Server capabilities
 */
package org.eclipse.lsp4xml;

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;
import static org.eclipse.lsp4xml.utils.VersionHelper.getVersion;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.eclipse.lsp4xml.commons.ParentProcessWatcher.ProcessLanguageServer;
import org.eclipse.lsp4xml.customservice.AutoCloseTagResponse;
import org.eclipse.lsp4xml.customservice.XMLCustomService;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4xml.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4xml.logs.LogHelper;
import org.eclipse.lsp4xml.services.IXMLDocumentProvider;
import org.eclipse.lsp4xml.services.XMLLanguageService;
import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.settings.AllXMLSettings;
import org.eclipse.lsp4xml.settings.InitializationOptionsSettings;
import org.eclipse.lsp4xml.settings.LogsSettings;
import org.eclipse.lsp4xml.settings.XMLGeneralClientSettings;
import org.eclipse.lsp4xml.settings.XMLExperimentalCapabilities;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.settings.capabilities.ServerCapabilitiesInitializer;
import org.eclipse.lsp4xml.settings.capabilities.XMLCapabilityManager;

/**
 * XML language server.
 *
 */
public class XMLLanguageServer
		implements LanguageServer, ProcessLanguageServer, XMLCustomService, IXMLDocumentProvider {

	private static final Logger LOGGER = Logger.getLogger(XMLLanguageServer.class.getName());

	private final XMLLanguageService xmlLanguageService;
	private final XMLTextDocumentService xmlTextDocumentService;
	private final XMLWorkspaceService xmlWorkspaceService;
	private LanguageClient languageClient;
	private final ScheduledExecutorService delayer;
	private Integer parentProcessId;
	public XMLCapabilityManager capabilityManager;


	public XMLLanguageServer() {
		xmlLanguageService = new XMLLanguageService();
		xmlLanguageService.setDocumentProvider(this);
		xmlTextDocumentService = new XMLTextDocumentService(this);
		xmlWorkspaceService = new XMLWorkspaceService(this);
		delayer = Executors.newScheduledThreadPool(1);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing LSP4XML server " + getVersion());
		this.parentProcessId = params.getProcessId();

		// Update XML language service extensions with InitializeParams
		xmlLanguageService.initializeParams(params);

		capabilityManager.setClientCapabilities(params.getCapabilities());
		updateSettings(InitializationOptionsSettings.getSettings(params));

		xmlTextDocumentService.updateClientCapabilities(capabilityManager.getClientCapabilities().capabilities);
		ServerCapabilities nonDynamicServerCapabilities = ServerCapabilitiesInitializer.getNonDynamicServerCapabilities(
				capabilityManager.getClientCapabilities(), xmlTextDocumentService.isIncrementalSupport());

		return CompletableFuture.completedFuture(new InitializeResult(nonDynamicServerCapabilities));
	}

	/*
	 * Registers all capabilities that do not support client side preferences to
	 * turn on/off
	 *
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.
	 * InitializedParams)
	 */
	@Override
	public void initialized(InitializedParams params) {
		capabilityManager.initializeCapabilities();
	}

	/**
	 * Update XML settings configured from the client.
	 * 
	 * @param initializationOptionsSettings the XML settings
	 */
	public void updateSettings(Object initializationOptionsSettings) {
		if (initializationOptionsSettings == null) {
			return;
		}
		// Update client settings
		initializationOptionsSettings = AllXMLSettings.getAllXMLSettings(initializationOptionsSettings);
		XMLGeneralClientSettings xmlClientSettings = XMLGeneralClientSettings.getGeneralXMLSettings(initializationOptionsSettings);
		if (xmlClientSettings != null) {
			// Update logs settings
			LogsSettings logsSettings = xmlClientSettings.getLogs();
			if (logsSettings != null) {
				LogHelper.initializeRootLogger(languageClient, logsSettings);
			}
			// Update format settings
			XMLFormattingOptions formatterSettings = xmlClientSettings.getFormat();
			if (formatterSettings != null) {
				xmlTextDocumentService.getSharedFormattingSettings().merge(formatterSettings);
			}

			CompletionSettings newCompletions = xmlClientSettings.getCompletion();
			if (newCompletions != null) {
				xmlTextDocumentService.updateCompletionSettings(newCompletions);
			}

			// Experimental capabilities
			XMLExperimentalCapabilities experimental = xmlClientSettings.getExperimental();
			if (experimental != null) {
				boolean incrementalSupport = experimental.getIncrementalSupport() != null
						&& experimental.getIncrementalSupport().getEnabled() != null
						&& experimental.getIncrementalSupport().getEnabled().booleanValue();
				xmlTextDocumentService.setIncrementalSupport(incrementalSupport);
			}
		}
		ContentModelSettings cmSettings = ContentModelSettings.getContentModelXMLSettings(initializationOptionsSettings);
		if(cmSettings != null) {
			XMLValidationSettings validationSettings = cmSettings.getValidation();
			xmlTextDocumentService.getValidationSettings().merge(validationSettings);

		}
		// Update XML language service extensions
		xmlTextDocumentService.updateSettings(initializationOptionsSettings);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return computeAsync(cc -> new Object());
	}

	@Override
	public void exit() {
		exit(0);
	}

	@Override
	public void exit(int exitCode) {
		delayer.shutdown();
		System.exit(exitCode);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return xmlTextDocumentService;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return xmlWorkspaceService;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = languageClient;
		capabilityManager = new XMLCapabilityManager(this.languageClient, xmlTextDocumentService, xmlWorkspaceService);
	}

	public LanguageClient getLanguageClient() {
		return languageClient;
	}

	public XMLLanguageService getXMLLanguageService() {
		return xmlLanguageService;
	}

	public ScheduledFuture<?> schedule(Runnable command, int delay, TimeUnit unit) {
		return delayer.schedule(command, delay, unit);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	@Override
	public CompletableFuture<AutoCloseTagResponse> closeTag(TextDocumentPositionParams params) {
		return computeAsync((monitor) -> {
			TextDocument document = xmlTextDocumentService.getDocument(params.getTextDocument().getUri());
			DOMDocument xmlDocument = xmlTextDocumentService.getXMLDocument(document);
			return getXMLLanguageService().doAutoClose(xmlDocument, params.getPosition());
		});
	}

	@Override
	public DOMDocument getDocument(String uri) {
		TextDocument document = xmlTextDocumentService.getDocument(uri);
		return document != null ? xmlTextDocumentService.getXMLDocument(document) : null;
	}

}
