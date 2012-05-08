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

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DosingPumpCommand;
import org.freemedsoftware.device.DosingPumpInterface;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.LabelPrinterInterface;
import org.freemedsoftware.device.PersistentJobStoreDAO;
import org.freemedsoftware.device.ShimDeviceManager;
import org.freemedsoftware.device.SignatureInterface;
import org.freemedsoftware.shim.exception.DeviceNotAvailableException;
import org.tmatesoft.sqljet.core.SqlJetException;

@WebService(endpointInterface = "org.freemedsoftware.shim.ShimService", serviceName = "Shim")
public class ShimServiceImpl implements ShimService {

	@Resource
	WebServiceContext context;

	static final Logger log = Logger.getLogger(ShimServiceImpl.class);

	@GET
	@Path("protocolversion")
	@Produces("application/json")
	@Override
	public Integer getProtocolVersion() {
		return PROTOCOL_VERSION;
	}

	@GET
	@Path("label/{printTemplate}/{printParameters}/{copyCount}")
	@Produces("application/json")
	@Override
	public Integer requestLabel(String printTemplate,
			HashMap<String, String> printParameters, Integer copyCount)
			throws DeviceNotAvailableException {
		ShimDeviceManager<LabelPrinterInterface> manager = MasterControlServlet
				.getLabelPrinterDeviceManager();
		if (manager == null) {
			throw new DeviceNotAvailableException();
		}
		JobStoreItem item = new JobStoreItem();
		Integer itemId = 0;
		item.setStatus(JobStoreItem.STATUS_NEW);
		item.setDevice(JobStoreItem.DEVICE_LABEL);
		item.setPrintTemplate(printTemplate);
		item.setPrintParameters(printParameters);
		try {
			itemId = PersistentJobStoreDAO.insert(item);
		} catch (SqlJetException e) {
			log.error(e);
			throw new DeviceNotAvailableException();
		}
		return itemId;
	}

	@GET
	@Path("signature/{displayInformation}")
	@Produces("application/json")
	@Override
	public Integer requestSignature(String displayInformation)
			throws DeviceNotAvailableException {
		ShimDeviceManager<SignatureInterface> manager = MasterControlServlet
				.getSignatureDeviceManager();
		if (manager == null) {
			throw new DeviceNotAvailableException();
		}
		JobStoreItem item = new JobStoreItem();
		Integer itemId = 0;
		item.setStatus(JobStoreItem.STATUS_NEW);
		item.setDevice(JobStoreItem.DEVICE_SIGNATURE);
		item.setDisplayText(displayInformation);
		try {
			itemId = PersistentJobStoreDAO.insert(item);
		} catch (SqlJetException e) {
			log.error(e);
			throw new DeviceNotAvailableException();
		}
		return itemId;
	}

	@GET
	@Path("dosing/{command}/{param}")
	@Produces("application/json")
	@Override
	public String requestDosingAction(DosingPumpCommand command, String param)
			throws Exception {
		ShimDeviceManager<DosingPumpInterface> manager = MasterControlServlet
				.getDosingPumpDeviceManager();
		if (manager == null) {
			throw new DeviceNotAvailableException();
		}
		if (command == DosingPumpCommand.DISPENSE) {
			return manager.getDeviceInstance().dispenseDose(
					Integer.parseInt(param));
		}
		if (command == DosingPumpCommand.GET_STATUS) {
			return manager.getDeviceInstance().getPumpStatus();
		}
		if (command == DosingPumpCommand.PRIME) {
			return manager.getDeviceInstance().primePump();
		}
		if (command == DosingPumpCommand.REVERSE) {
			return manager.getDeviceInstance().reversePump();
		}

		if (command != null) {
			throw new Exception("Invalid command given!");
		} else {
			return "NULL";
		}
	}

	@GET
	@Path("jobstatus/{requestId}")
	@Produces("application/json")
	@Override
	public SignatureStatus getJobStatus(Integer requestId) throws Exception {
		JobStoreItem item = PersistentJobStoreDAO.get(requestId);
		return SignatureStatus.fromString(item.getStatus());
	}

	@GET
	@Path("jobitem/{requestId}")
	@Produces("application/json")
	@Override
	public JobStoreItem getJobItem(Integer requestId) throws Exception {
		JobStoreItem item = PersistentJobStoreDAO.get(requestId);
		return item;
	}

	@GET
	@Path("devices")
	@Produces("application/json")
	@Override
	public List<ShimDeviceInformation> getDevices() {
		return null;
	}

	/*
	 * @GET
	 * 
	 * @Path("testmap")
	 * 
	 * @Produces("application/json")
	 * 
	 * @Override public HashMap<String, String> getTestMap() { HashMap<String,
	 * String> test = new HashMap<String, String>(); test.put("key_a",
	 * "value_a"); test.put("key_b", "value_b"); test.put("key_c", "value_c");
	 * return test; }
	 */

}