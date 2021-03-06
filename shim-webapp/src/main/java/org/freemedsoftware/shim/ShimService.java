/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management System
 * Copyright (C) 1999-2012 FreeMED Software Foundation
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
 * Foundation, Inc., 51 Franklin St, Suite 500, Boston, MA 02110, USA.
 */

package org.freemedsoftware.shim;

import java.util.HashMap;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.PathParam;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.freemedsoftware.device.DosingPumpCommand;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.shim.exception.DeviceNotAvailableException;

@WebService
public interface ShimService {

	public static int PROTOCOL_VERSION = 2;

	public Integer getProtocolVersion();

	public Integer requestLabel(
			@PathParam("printTemplate") @WebParam(name = "printTemplate") String printTemplate,
			@PathParam("printParameters") @WebParam(name = "printParameters") @XmlJavaTypeAdapter(HashMapXmlAdapter.class) HashMap<String, String> printParameters,
			@PathParam("copyCount") @WebParam(name = "copyCount") Integer copyCount)
			throws DeviceNotAvailableException;

	public Integer requestSignature(
			@PathParam("displayInformation") @WebParam(name = "displayInformation") String displayInformation)
			throws DeviceNotAvailableException;

	public SignatureStatus getJobStatus(
			@PathParam("requestId") @WebParam(name = "requestId") Integer requestId)
			throws Exception;

	public JobStoreItem getJobItem(
			@PathParam("requestId") @WebParam(name = "requestId") Integer requestId)
			throws Exception;

	public String requestDosingAction(
			@PathParam("command") @WebParam(name = "command") DosingPumpCommand command,
			@PathParam("param") @WebParam(name = "param") String param)
			throws Exception;

	/*
	 * public @XmlJavaTypeAdapter(HashMapXmlAdapter.class) HashMap<String,
	 * String> getTestMap();
	 */

	public List<ShimDeviceInformation> getDevices();

}
