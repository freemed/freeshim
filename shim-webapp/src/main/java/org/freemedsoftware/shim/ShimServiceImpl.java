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

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.freemedsoftware.shim.exception.DeviceNotAvailableException;

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
	public Integer requestSignature(@WebParam(name = "device") String device)
			throws DeviceNotAvailableException {
		return null;
	}

	@GET
	@Path("signaturestatus/{requestId}")
	@Produces("application/json")
	@Override
	public SignatureStatus getSignatureStatus(
			@WebParam(name = "requestId") Integer requestId) {
		return null;
	}

}
