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

import java.util.HashMap;
import java.util.List;

import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.DeviceInterface;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.ShimDevice;

@ShimDevice(name = "Dummy Shim", capability = DeviceCapability.DEVICE_SIGNATURE_TABLET)
public class DummyShim implements DeviceInterface {

	@Override
	public void close() throws Exception {

	}

	@Override
	public void configure(HashMap<String, Object> config) {

	}

	@Override
	public void init() throws Exception {

	}

	@Override
	public List<String> getConfigurationOptions() {
		return null;
	}

	@Override
	public boolean isProcessing() {
		return false;
	}

	@Override
	public boolean initJobRequest(JobStoreItem item) throws Exception {
		return false;
	}

}
