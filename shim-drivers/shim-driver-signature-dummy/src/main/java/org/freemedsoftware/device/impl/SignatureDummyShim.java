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

package org.freemedsoftware.device.impl;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.ShimDevice;
import org.freemedsoftware.device.SignatureInterface;

@ShimDevice(name = "Signature Dummy Shim", capability = DeviceCapability.DEVICE_SIGNATURE_TABLET)
public class SignatureDummyShim implements SignatureInterface {

	protected Logger log = Logger.getLogger(DummyShim.class);

	@Override
	public void init() {
		log.info("open/init dummy sig device");
	}

	@Override
	public boolean initSignatureRequest(String uid) throws Exception {
		return false;
	}

	@Override
	public void close() throws Exception {
		log.info("close dummy sig device");
	}

	@Override
	public void configure(HashMap<String, Object> config) {
		log.info("configure dummy sig device");
	}

}
