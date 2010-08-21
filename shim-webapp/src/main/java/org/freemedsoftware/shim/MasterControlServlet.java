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

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.log4j.Logger;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.PersistentJobStoreDAO;
import org.freemedsoftware.device.ShimDeviceManager;
import org.freemedsoftware.device.SignatureInterface;
import org.tmatesoft.sqljet.core.SqlJetException;

public class MasterControlServlet extends HttpServlet {

	private static final long serialVersionUID = 672124984404116767L;

	static final Logger logger = Logger.getLogger(MasterControlServlet.class);

	protected final int THREAD_SLEEP_TIME = 250;

	protected CompositeConfiguration config = null;

	protected static ShimDeviceManager<SignatureInterface> sManager = null;

	protected Timer timer = new Timer();

	public void init() throws ServletException {
		logger.info("MasterControlServlet initializing");

		logger.info("Loading configuration");
		config = Configuration.getConfiguration();

		String jobstoreLocation = config.getString("jobstore.location");

		logger.info("Initializing connection to persistent job store : "
				+ jobstoreLocation);
		if (new File(jobstoreLocation).exists()) {
			logger.info("Found existing job store, no need to initialize");
		} else {
			try {
				PersistentJobStoreDAO.create(jobstoreLocation);
			} catch (SqlJetException ex) {
				throw new ServletException(
						"Unable to open persistent job store");
			}
		}

		launchWorkerThreads();

		logger.info("MasterControlServlet init finished");
	}

	public void launchWorkerThreads() {
		logger.info("Launching worker threads");

		String signatureDriver = config.getString("driver.signature");
		if (signatureDriver != null) {
			logger.info("Initializing signature pad driver " + signatureDriver);
			try {
				sManager = new ShimDeviceManager<SignatureInterface>(
						signatureDriver);
				sManager.init();
			} catch (Exception e) {
				logger.error(e);
			}
		} else {
			logger.warn("No signature pad driver specified, skipping.");
		}

		logger.info("Launching job store scheduler timer");
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				while (!Thread.interrupted()) {
					try {
						Thread.sleep(THREAD_SLEEP_TIME);
						scanForJobs();
					} catch (InterruptedException e) {
						logger.warn(e);
					}
				}
			}

			protected void scanForJobs() {
				JobStoreItem signatureItem = null;
				JobStoreItem vitalsItem = null;
				try {
					List<JobStoreItem> items = PersistentJobStoreDAO
							.unassignedJobs();
					Iterator<JobStoreItem> iter = items.iterator();
					while (iter.hasNext() && signatureItem == null
							&& vitalsItem == null) {
						JobStoreItem thisItem = iter.next();
						if (thisItem.getDevice().equalsIgnoreCase(
								JobStoreItem.DEVICE_SIGNATURE)
								&& signatureItem == null) {
							signatureItem = thisItem;
						}
						if (thisItem.getDevice().equalsIgnoreCase(
								JobStoreItem.DEVICE_VITALS)
								&& signatureItem == null) {
							vitalsItem = thisItem;
						}
					}
				} catch (SqlJetException e) {
					logger.error(e);
				}

				if (signatureItem != null && sManager != null) {
					logger.info("Found signature item to be processed (id = "
							+ signatureItem.getId() + ")");
					if (!sManager.getDeviceInstance().isProcessing()) {
						try {
							sManager.getDeviceInstance().initSignatureRequest(
									signatureItem);

							// Update with pending status
							signatureItem
									.setStatus(JobStoreItem.STATUS_PENDING);
							PersistentJobStoreDAO.update(signatureItem);
						} catch (Exception e) {
							logger.error(e);
						}
					} else {
						logger
								.warn("Device is processing, skipping new job load");
					}
				}

			}

		}, THREAD_SLEEP_TIME, THREAD_SLEEP_TIME);
	}

	public static ShimDeviceManager<SignatureInterface> getSignatureDeviceManager() {
		return sManager;
	}

	@Override
	public void destroy() {
		if (sManager != null) {
			try {
				sManager.close();
			} catch (Exception e) {
				logger.warn(e);
			}
		}
		super.destroy();
	}

}
