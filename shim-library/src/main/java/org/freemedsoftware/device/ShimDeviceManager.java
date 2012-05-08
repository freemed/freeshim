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

import java.util.Set;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.base.Predicate;

public class ShimDeviceManager<T extends DeviceInterface> implements Runnable {

	protected static Logger log = Logger.getLogger(ShimDeviceManager.class);

	protected T deviceInstance = null;

	protected Thread persistentThread = null;

	protected boolean active = false;

	protected String className = null;

	public ShimDeviceManager() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
	}

	public boolean getActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@SuppressWarnings("unchecked")
	public ShimDeviceManager(String className) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		if (className == null) {
			throw new InstantiationException("no class name given");
		}
		setClassName(className);
		deviceInstance = (T) Class.forName(className).newInstance();
	}

	public void scanShimDevices() {
		Predicate<String> filter = new FilterBuilder()
				.include("org.freemedsoftware.device.impl.*");

		Reflections r = new Reflections(
				new ConfigurationBuilder()
						.filterInputsBy(
								new FilterBuilder.Include(FilterBuilder
										.prefix("org.freemedsoftware.device")))
						.setUrls(
								ClasspathHelper
										.getUrlsForPackagePrefix("org.freemedsoftware.device"))
						.setScanners(new SubTypesScanner(),
								new TypeAnnotationsScanner(),
								new ResourcesScanner()));
		/*
		 * Reflections r = new Reflections( new ConfigurationBuilder()
		 * .filterInputsBy(filter) .setScanners( new
		 * SubTypesScanner().filterResultsBy(filter), new
		 * TypeAnnotationsScanner() .filterResultsBy(filter), new
		 * FieldAnnotationsScanner() .filterResultsBy(filter), new
		 * MethodAnnotationsScanner() .filterResultsBy(filter), new
		 * ConvertersScanner().filterResultsBy(filter)) .setUrls(
		 * ClasspathHelper
		 * .getUrlsForPackagePrefix("org.freemedsoftware.device.impl")));
		 */
		Set<Class<?>> s = r.getTypesAnnotatedWith(ShimDevice.class);
		System.out.println("scanShimDevices found " + s.size() + " entries");
		for (Class<?> c : s) {
			System.out.println("canonical name: " + c.getCanonicalName());
		}
	}

	/**
	 * Initialize child device.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		if (deviceInstance == null) {
			throw new Exception("device not initialized");
		}
		if (deviceInstance instanceof SignatureInterface) {
			initSignatureDevice();
		}
		if (deviceInstance instanceof LabelPrinterInterface) {
			initLabelPrinterDevice();
		}
		if (deviceInstance instanceof DosingPumpInterface) {
			initDosingDevice();
		}

		// Set active to true
		setActive(true);
	}

	/**
	 * Get internal device driver instance.
	 * 
	 * @return
	 */
	public T getDeviceInstance() {
		return deviceInstance;
	}

	protected void initDosingDevice() throws Exception {
		log.info("initDosingDevice()");
		DosingPumpInterface sDevice = (DosingPumpInterface) deviceInstance;
		log.info("Running device init");
		sDevice.init();
		persistentThread = new Thread(this);
	}

	protected void initSignatureDevice() throws Exception {
		log.info("initSignatureDevice()");
		SignatureInterface sDevice = (SignatureInterface) deviceInstance;
		log.info("Running device init");
		sDevice.init();
		persistentThread = new Thread(this);
	}

	protected void initLabelPrinterDevice() throws Exception {
		log.info("initSignatureDevice()");
		LabelPrinterInterface lDevice = (LabelPrinterInterface) deviceInstance;
		log.info("Running device init");
		lDevice.init();
		persistentThread = new Thread(this);
	}

	/**
	 * Closes child device if open.
	 * 
	 * @throws Exception
	 */
	public void close() throws Exception {
		if (deviceInstance != null) {
			deviceInstance.close();
		}
		if (persistentThread != null) {
			persistentThread.interrupt();
		}
		setActive(false);
	}

	@Override
	public void run() {
	}

}
