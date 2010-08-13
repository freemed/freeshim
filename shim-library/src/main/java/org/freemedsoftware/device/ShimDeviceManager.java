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

import static java.util.Arrays.asList;

import java.util.Set;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ConvertersScanner;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.base.Predicate;

public class ShimDeviceManager<T extends DeviceInterface> {

	protected static Logger log = Logger.getLogger(ShimDeviceManager.class);

	protected T deviceInstance = null;

	protected Thread persistentThread = null;

	public ShimDeviceManager() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
	}

	@SuppressWarnings("unchecked")
	public ShimDeviceManager(String className) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		deviceInstance = (T) Class.forName(className).newInstance();
	}

	public void scanShimDevices() {
		Predicate<String> filter = new FilterBuilder()
				.include("org.freemedsoftware.device.ShimDevice\\$.*");

		Reflections r = new Reflections(
				new ConfigurationBuilder().filterInputsBy(filter).setScanners(
						new SubTypesScanner().filterResultsBy(filter),
						new TypeAnnotationsScanner().filterResultsBy(filter),
						new FieldAnnotationsScanner().filterResultsBy(filter),
						new MethodAnnotationsScanner().filterResultsBy(filter),
						new ConvertersScanner().filterResultsBy(filter))
						.setUrls(
								asList(ClasspathHelper
										.getUrlForClass(ShimDevice.class))));
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
	}

	protected void initSignatureDevice() throws Exception {
		SignatureInterface sDevice = (SignatureInterface) deviceInstance;
		sDevice.init();
		persistentThread = new Thread() {

		};
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
	}

}
