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

package org.freemedsoftware.device;

import gnu.io.CommPortIdentifier;
import gnu.io.CommPortOwnershipListener;
import gnu.io.NoSuchPortException;
import gnu.io.ParallelPort;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class ParallelPrinterInterface implements CommPortOwnershipListener,
		Runnable {

	public enum PortStatus {
		OPEN, CLOSED, ERROR
	};

	static final Logger log = Logger.getLogger(ParallelPrinterInterface.class);

	protected OutputStream out = null;
	protected ParallelPort parallelPort = null;
	protected PortStatus portStatus = PortStatus.CLOSED;

	public ParallelPrinterInterface() {
	}

	public void open(String portName, int timeout) throws NoSuchPortException,
			PortInUseException, UnsupportedCommOperationException, IOException {
		if (portStatus != PortStatus.OPEN) {
			CommPortIdentifier portId = CommPortIdentifier
					.getPortIdentifier(portName);
			portId.addPortOwnershipListener(this);
			parallelPort = (ParallelPort) portId.open("Parallel", timeout);
			parallelPort.setMode(ParallelPort.LPT_MODE_ANY);
			out = parallelPort.getOutputStream();
			portStatus = PortStatus.OPEN;
		}
	}

	public OutputStream getOutputStream() {
		return out;
	}

	public void write(String data) throws IOException {
		log.info("Attempting write for " + data.length() + " bytes");
		byte[] b = data.getBytes("ASCII");
		for (int iter = 0; iter < b.length; iter++) {
			log.trace("Pushing byte #" + iter + " (" + b[iter] + ")");
			out.write(b[iter]);
		}
		out.flush();
	}

	public void close() {
		if (portStatus == PortStatus.OPEN) {
			portStatus = PortStatus.CLOSED;
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}

	@Override
	public void ownershipChange(int type) {
		switch (type) {
		case CommPortOwnershipListener.PORT_OWNED:
			log.info("We got the port");
			break;
		case CommPortOwnershipListener.PORT_UNOWNED:
			log.info("We've just lost our port ownership");
			break;
		case CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED:
			log.info("Someone is asking our port's ownership");
			break;
		}
	}

	@Override
	public void run() {
		// TODO: open
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.warn(e);
				// TODO: close
				log.info("Exiting child thread.");
			}
		}
	}

}
