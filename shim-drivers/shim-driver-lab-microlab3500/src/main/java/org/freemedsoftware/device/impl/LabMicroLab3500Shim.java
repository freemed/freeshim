/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management System
 * Copyright (C) 1999-2011 FreeMED Software Foundation
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

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.LabSerialInterface;
import org.freemedsoftware.device.ShimDevice;

@ShimDevice(name = "MicroLab 3500 Lab Shim", capability = DeviceCapability.DEVICE_VITAL_SIGNS)
public class LabMicroLab3500Shim extends LabSerialInterface {

	protected Logger log = Logger.getLogger(LabMicroLab3500Shim.class);

	public LabMicroLab3500Shim() {
		setConfigName("org.freemedsoftware.device.impl.LabMicroLab3500Shim");
	}

	@Override
	public void init() throws Exception {
		super.init();

		// Any additional config goes here
	}

}
