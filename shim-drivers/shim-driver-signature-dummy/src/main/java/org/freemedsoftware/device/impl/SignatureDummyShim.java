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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.PersistentJobStoreDAO;
import org.freemedsoftware.device.ShimDevice;
import org.freemedsoftware.device.SignatureInterface;
import org.tmatesoft.sqljet.core.SqlJetException;

@ShimDevice(name = "Signature Dummy Shim", capability = DeviceCapability.DEVICE_SIGNATURE_TABLET)
public class SignatureDummyShim implements SignatureInterface {

	protected Logger log = Logger.getLogger(DummyShim.class);

	protected Timer timer = new Timer();

	protected Integer jobId = null;

	protected JobStoreItem item = null;

	public final static int PROCESSING_LENGTH = 5000;

	protected class ProcessingTimerTask extends TimerTask {

		@Override
		public void run() {
			item.setStatus(JobStoreItem.STATUS_COMPLETED);
			try {
				PersistentJobStoreDAO.update(item);
			} catch (SqlJetException e) {
				log.error(e);
			}

			// Clear job and item objects
			jobId = null;
			item = null;
		}

	}

	@Override
	public void init() {
		log.info("open/init dummy sig device");
	}

	@Override
	public boolean initSignatureRequest(JobStoreItem item) throws Exception {
		jobId = item.getId();

		// Update status to "PENDING" (processing)
		item.setStatus(JobStoreItem.STATUS_PENDING);
		try {
			PersistentJobStoreDAO.update(item);
		} catch (SqlJetException e) {
			log.error(e);
		}

		this.item = item;
		timer.schedule(new ProcessingTimerTask(), PROCESSING_LENGTH);
		return true;
	}

	@Override
	public void close() throws Exception {
		log.info("close dummy sig device");
		timer.cancel();
	}

	@Override
	public void configure(HashMap<String, Object> config) {
		log.info("configure dummy sig device");
	}

	@Override
	public List<String> getConfigurationOptions() {
		return null;
	}

	@Override
	public boolean isProcessing() {
		return (jobId != null);
	}

}
