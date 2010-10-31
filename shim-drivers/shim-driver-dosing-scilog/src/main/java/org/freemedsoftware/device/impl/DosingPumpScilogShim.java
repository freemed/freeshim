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

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.DosingPumpSerialInterface;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.SerialInterface;
import org.freemedsoftware.device.ShimDevice;

@ShimDevice(name = "Scilog Dosing Pump Shim", capability = DeviceCapability.DEVICE_DOSING_PUMP)
public class DosingPumpScilogShim extends DosingPumpSerialInterface {

	protected Logger log = Logger.getLogger(DosingPumpScilogShim.class);

	protected Timer timer = new Timer();

	protected Integer jobId = null;

	protected JobStoreItem item = null;

	public final static int PROCESSING_LENGTH = 5000;

	public DosingPumpScilogShim() {
		setConfigName("org.freemedsoftware.device.impl.DosingPumpScilogShim");
	}

	/**
	 * Raw transport of commands to pump. Should not be called directly, but
	 * should be used by internal commands.
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 * @throws PortInUseException
	 * @throws UnsupportedCommOperationException
	 * @throws NoSuchPortException
	 */
	protected String sendCommandToPump(String cmd) throws IOException,
			PortInUseException, UnsupportedCommOperationException,
			NoSuchPortException {
		if (cmd == null) {
			log.error("Null cmd given!");
			throw new IOException("Null cmd given!");
		}
		if (serialInterface == null) {
			log.error("Serial interface not initialized!");
			throw new IOException("Serial interface not initialized!");
		}
		log.info("command to pump is " + cmd);
		serialInterface.write(cmd);

		// Consume first line as response from pump
		log.info("Reading response.");
		String out = serialInterface.read().trim();
		if (out.indexOf(SerialInterface.CR) != -1) {
			return out.substring(0, out.indexOf(SerialInterface.CR)).trim();
		} else if (out.indexOf(SerialInterface.LF) != -1) {
			return out.substring(0, out.indexOf(SerialInterface.LF)).trim();
		}
		return out.trim();
	}

	@Override
	public void clearPumpForClosing() throws Exception {
		sendCommandToPump("E");
	}

	@Override
	public void clearPumpForOpening() throws Exception {
		sendCommandToPump("P");
	}

	@Override
	public String dispenseDose(Integer units) throws Exception {
		return sendCommandToPump("V" + units.toString());
	}

	@Override
	public String getPumpStatus() throws Exception {
		return sendCommandToPump("S");
	}

	@Override
	public Integer getPumpTimeInterval() throws Exception {
		String x = sendCommandToPump("T");
		return Integer.parseInt(x);
	}

	@Override
	public void primePump() throws Exception {
		sendCommandToPump("P");
	}

	@Override
	public void setPumpTimeInterval(Integer interval) throws Exception {
		sendCommandToPump("C" + interval.toString());
	}

}
