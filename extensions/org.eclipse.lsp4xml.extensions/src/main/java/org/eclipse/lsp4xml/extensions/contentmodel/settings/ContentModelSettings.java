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
package org.eclipse.lsp4xml.extensions.contentmodel.settings;

import org.eclipse.lsp4xml.utils.JSONUtility;

/**
 * Content model settings.
 *
 */
public class ContentModelSettings {

	private Boolean useCache;

	private String[] catalogs;

	private XMLFileAssociation[] fileAssociations;

	private XMLValidationSettings validation;

	/**
	 * Returns true if cache to download XML Schema, DTD must be activated and false
	 * otherwise.
	 * 
	 * @return true if cache to download XML Schema, DTD must be activated and false
	 *         otherwise.
	 */
	public Boolean isUseCache() {
		return useCache;
	}

	/**
	 * Set cache to true if cache to download XML Schema, DTD must be activated and
	 * false otherwise.
	 * 
	 * @param useCache the use cache.
	 */
	public void setUseCache(Boolean useCache) {
		this.useCache = useCache;
	}

	/**
	 * Register the list of the XML catalogs file path.
	 * 
	 * @param catalogs
	 */
	public void setCatalogs(String[] catalogs) {
		this.catalogs = catalogs;
	}

	/**
	 * Returns the list of the XML catalogs file path.
	 * 
	 * @return the list of the XML catalogs file path.
	 */
	public String[] getCatalogs() {
		return catalogs;
	}

	public void setFileAssociations(XMLFileAssociation[] fileAssociations) {
		this.fileAssociations = fileAssociations;
	}

	/**
	 * Returns file associations list between a file name pattern (glob) and an XML
	 * Schema file, DTD (system Id).
	 * 
	 * @return file associations list between a file name pattern (glob) and an XML
	 *         Schema file, DTD (system Id).
	 */
	public XMLFileAssociation[] getFileAssociations() {
		return fileAssociations;
	}

	public static ContentModelSettings getContentModelXMLSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, ContentModelSettings.class);
	}

	public void setValidation(XMLValidationSettings validation) {
		this.validation = validation;
	}

	public XMLValidationSettings getValidation() {
		return validation;
	}

}
