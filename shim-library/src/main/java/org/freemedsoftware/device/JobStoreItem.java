/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management System
 * Copyright (C) 1999-2010 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.freemedsoftware.device;

import java.util.HashMap;
import java.util.Map;

public class JobStoreItem {

	/**
	 * Status for completed jobs.
	 */
	public final static String STATUS_COMPLETED = "COMPLETE";

	/**
	 * Status for jobs which have run but returned errors.
	 */
	public final static String STATUS_ERROR = "ERROR";

	/**
	 * Status for jobs which are currently being processed.
	 */
	public final static String STATUS_PENDING = "PENDING";

	/**
	 * Status for unassigned jobs.
	 */
	public final static String STATUS_NEW = "NEW";

	public final static String DEVICE_LABEL = "LABEL";
	public final static String DEVICE_SIGNATURE = "SIGNATURE";
	public final static String DEVICE_VITALS = "VITALS";

	private Integer id;

	private String status;

	private String device;

	private String displayText;

	private String printTemplate = null;

	private Map<String, String> printParameters = new HashMap<String, String>();

	private Integer printCount = 1;

	private byte[] signatureRaw;

	private byte[] signatureImage;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getDevice() {
		return device;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setPrintTemplate(String printTemplate) {
		this.printTemplate = printTemplate;
	}

	public String getPrintTemplate() {
		return printTemplate;
	}

	public void setPrintParameters(Map<String, String> printParameters) {
		this.printParameters = printParameters;
	}

	public Map<String, String> getPrintParameters() {
		return printParameters;
	}

	public void setPrintCount(Integer printCount) {
		this.printCount = printCount;
	}

	public Integer getPrintCount() {
		return printCount;
	}

	public void setSignatureRaw(byte[] signatureRaw) {
		this.signatureRaw = signatureRaw;
	}

	public byte[] getSignatureRaw() {
		return signatureRaw;
	}

	public void setSignatureImage(byte[] signatureImage) {
		this.signatureImage = signatureImage;
	}

	public byte[] getSignatureImage() {
		return signatureImage;
	}

}
