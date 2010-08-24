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

package org.freemedsoftware.shim;

import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.JobStoreItem;
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
	@Path("requestsignature/{device}")
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

}