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

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.DosingPumpInterface;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.SerialInterface;
import org.freemedsoftware.device.ShimDevice;

@ShimDevice(name = "Scilog Dosing Pump Shim", capability = DeviceCapability.DEVICE_DOSING_PUMP)
public class DosingPumpScilogShim implements DosingPumpInterface {

	protected Logger log = Logger.getLogger(DosingPumpScilogShim.class);

	protected Timer timer = new Timer();

	protected Integer jobId = null;

	protected JobStoreItem item = null;

	public final static int PROCESSING_LENGTH = 5000;

	protected HashMap<String, Object> config = new HashMap<String, Object>();

	protected AtomicBoolean processing = new AtomicBoolean();

	protected SerialInterface serialInterface = null;

	@Override
	public void close() throws Exception {
		if (serialInterface != null) {
			serialInterface.close();
		}
	}

	@Override
	public void configure(HashMap<String, Object> config) {
		log.info("Loading configuration");
		this.config = config;
	}

	@Override
	public List<String> getConfigurationOptions() {
		return Arrays.asList(new String[] { "scilog.port", "scilog.baud",
				"scilog.timeout" });
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		// Initially, set to "processing" so we don't have pigpiles.
		processing.set(true);

		// log.info("Changing java.library.path for rxtx");
		// System.setProperty("java.library.path", getRealPath("/WEB-INF/lib"));

		if (config.get("scilog.enabled").equals("false")) {
			throw new Exception("Skipping dosing pump, scilog.enabled=false");
		}

		// Display available ports
		log.info("Scanning for available ports");
		Enumeration portList = (Enumeration) CommPortIdentifier
				.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList
					.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Found port: " + portId.getName());
			}
		}

		serialInterface = new SerialInterface();
		log.info("opening pump.port = " + config.get("scilog.port"));
		try {
			serialInterface.open((String) config.get("scilog.port"), Integer
					.parseInt((String) config.get("scilog.baud")), Integer
					.parseInt((String) config.get("scilog.timeout")));
		} catch (NoSuchPortException e) {
			log.error(e);
		} catch (PortInUseException e) {
			log.error(e);
		} catch (UnsupportedCommOperationException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		log.info("Pump opened");

		// Unlock when we're ready to go.
		processing.set(false);
	}

	@Override
	public boolean initJobRequest(JobStoreItem item) throws Exception {
		log
				.warn("Currently unused (initJobRequest), as communication is synchronous");
		return false;
	}

	@Override
	public boolean isProcessing() {
		return processing.get();
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
