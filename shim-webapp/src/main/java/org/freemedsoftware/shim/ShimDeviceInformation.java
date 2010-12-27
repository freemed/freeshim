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

package org.freemedsoftware.shim;

import java.io.Serializable;

import org.freemedsoftware.device.DeviceCapability;

public class ShimDeviceInformation implements Serializable {

	private static final long serialVersionUID = 7325861056406660405L;

	private String deviceName;

	private String deviceDriver;

	private DeviceCapability deviceType;

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceDriver(String deviceDriver) {
		this.deviceDriver = deviceDriver;
	}

	public String getDeviceDriver() {
		return deviceDriver;
	}

	public void setDeviceType(DeviceCapability deviceType) {
		this.deviceType = deviceType;
	}

	public DeviceCapability getDeviceType() {
		return deviceType;
	}

}
