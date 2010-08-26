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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.ParallelPrinterInterface;
import org.freemedsoftware.device.PersistentJobStoreDAO;
import org.freemedsoftware.device.ShimDevice;
import org.freemedsoftware.device.SignatureInterface;
import org.tmatesoft.sqljet.core.SqlJetException;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@ShimDevice(name = "Label Printer ESC-POS Shim", capability = DeviceCapability.DEVICE_LABEL_PRINTER)
public class LabelEscPosShim implements SignatureInterface {

	protected Logger log = Logger.getLogger(LabelEscPosShim.class);

	protected Timer timer = new Timer();

	protected Integer jobId = null;

	protected JobStoreItem item = null;

	public final static int PROCESSING_LENGTH = 5000;

	protected HashMap<String, Object> config = new HashMap<String, Object>();

	protected ParallelPrinterInterface printerInterface = null;

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
	public void init() throws Exception {
		log.info("Open label printer device");

		if (config == null) {
			log.error("Config has not been set yet! Bombing out.");
			return;
		}

		String portName = (String) config.get("escpos.port");
		Integer timeout = Integer.parseInt((String) config
				.get("escpos.timeout"));

		log.info("Creating interface for " + portName + " with timeout "
				+ timeout);

		printerInterface = new ParallelPrinterInterface();
		printerInterface.open(portName, timeout);
	}

	@Override
	public boolean initJobRequest(JobStoreItem item) throws Exception {
		jobId = item.getId();

		// Update status to "PENDING" (processing)
		item.setStatus(JobStoreItem.STATUS_PENDING);
		try {
			PersistentJobStoreDAO.update(item);
		} catch (SqlJetException e) {
			log.error(e);
		}

		this.item = item;

		// Process item
		printLabel(item.getPrintTemplate(), item.getPrintParameters(), item
				.getPrintCount());

		// Clear everything
		this.item = null;
		this.jobId = null;

		return true;
	}

	@Override
	public void close() throws Exception {
		log.info("Close label printer");
		timer.cancel();
		if (printerInterface != null) {
			printerInterface.close();
		}
	}

	@Override
	public void configure(HashMap<String, Object> config) {
		this.config = config;
	}

	@Override
	public List<String> getConfigurationOptions() {
		return null;
	}

	@Override
	public boolean isProcessing() {
		return (jobId != null);
	}

	protected int printLabel(String printTemplate, Map<String, String> values,
			Integer copies) throws IOException {
		// Get template configuration
		freemarker.template.Configuration cfg = this
				.getFreemarkerConfiguration();

		// Set values
		if (values == null) {
			values = new HashMap<String, String>();
		}
		values.put("esc", "\u001b");

		Template template = cfg.getTemplate(printTemplate);

		// Write template
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(os);
		for (int iter = 0; iter < copies; iter++) {
			try {
				log.info("Writing label copy " + (iter + 1) + " of " + copies);
				template.process(values, out);
				os.flush();
				log.debug("Got " + os.size() + " bytes");
				printerInterface.write(os.toString());
				log.trace("LABEL: " + os.toString());
				log.info("Write to printer device completed.");
				out.flush();
			} catch (TemplateException e) {
				log.error(e);
				return 0;
			}
		}

		// Return successful print
		return 1;
	}

	protected freemarker.template.Configuration getFreemarkerConfiguration()
			throws IOException {
		freemarker.template.Configuration cfg = new freemarker.template.Configuration();
		cfg.setDirectoryForTemplateLoading(new File(this.getClass()
				.getClassLoader().getResource("/templates").toExternalForm()));
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		return cfg;
	}

}
