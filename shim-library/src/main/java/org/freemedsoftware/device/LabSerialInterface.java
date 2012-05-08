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

package org.freemedsoftware.device;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public abstract class LabSerialInterface implements DeviceInterface {

	protected Logger log = Logger.getLogger(LabSerialInterface.class);

	protected HashMap<String, Object> config = new HashMap<String, Object>();

	protected AtomicBoolean processing = new AtomicBoolean();

	protected SerialInterface serialInterface = null;

	protected String configName = null;

	public String getConfigName() {
		return this.configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	@Override
	public void close() throws Exception {
		if (serialInterface != null) {
			log.info("Closing serial port for lab device");
			serialInterface.close();
		}
	}

	@Override
	public void configure(HashMap<String, Object> config) {
		this.config = config;
	}

	@Override
	public List<String> getConfigurationOptions() {
		return Arrays.asList(new String[] { getConfigName() + ".enabled",
				getConfigName() + ".port", getConfigName() + ".baud",
				getConfigName() + ".timeout" });
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		// Initially, set to "processing" so we don't have pigpiles.
		processing.set(true);

		if (config.get(getConfigName() + ".enabled").equals("false")) {
			throw new Exception("Skipping lab, " + getConfigName()
					+ ".enabled=false");
		}

		// Display available ports
		log.info("Scanning for available ports");
		Enumeration portList = (Enumeration) CommPortIdentifier
				.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList
					.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				log.debug("Found port: " + portId.getName());
			}
		}

		serialInterface = new SerialInterface();
		log
				.info("opening pump.port = "
						+ config.get(getConfigName() + ".port"));
		try {
			serialInterface.open(
					(String) config.get(getConfigName() + ".port"), Integer
							.parseInt((String) config.get(getConfigName()
									+ ".baud")), Integer
							.parseInt((String) config.get(getConfigName()
									+ ".timeout")));
		} catch (NoSuchPortException e) {
			log.error(e);
			throw e;
		} catch (PortInUseException e) {
			log.error(e);
			throw e;
		} catch (UnsupportedCommOperationException e) {
			log.error(e);
			throw e;
		} catch (IOException e) {
			log.error(e);
			throw e;
		}
		log.info("Lab device opened");

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

}
