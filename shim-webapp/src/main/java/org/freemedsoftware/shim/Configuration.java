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

package org.freemedsoftware.shim;

import java.io.File;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

public class Configuration {

	public static String DEFAULT_CONFIG = "/WEB-INF/shim-default.properties";
	public static String OVERRIDE_CONFIG = System
			.getProperty("shim.properties");

	protected static CompositeConfiguration compositeConfiguration = null;

	static final Logger log = Logger.getLogger(Configuration.class);

	protected static MasterControlServlet servletContext = null;

	/**
	 * Get servlet object.
	 * 
	 * @return
	 */
	public static MasterControlServlet getServletContext() {
		return servletContext;
	}

	/**
	 * Store servlet object.
	 * 
	 * @param hS
	 */
	public static void setServletContext(MasterControlServlet hS) {
		servletContext = hS;
	}

	/**
	 * Get current global configuration object.
	 * 
	 * @return
	 */
	public static CompositeConfiguration getConfiguration() {
		if (compositeConfiguration == null) {
			Configuration.loadConfiguration();
		}
		if (compositeConfiguration == null) {
			log
					.error("Should never be null here, configuration is failing to load!");
		}
		return compositeConfiguration;
	}

	/**
	 * Load configuration from both template and override properties files.
	 */
	public static void loadConfiguration() {
		log.trace("Entered loadConfiguration");
		if (servletContext == null) {
			log.error("servletContext not set!");
		}
		if (compositeConfiguration == null) {
			log.info("Configuration object not present, instantiating");
			compositeConfiguration = new CompositeConfiguration();

			PropertiesConfiguration defaults = null;
			try {
				defaults = new PropertiesConfiguration(servletContext
						.getServletContext().getRealPath(DEFAULT_CONFIG));
				log.info("Loading default configuration from "
						+ servletContext.getServletContext().getRealPath(
								DEFAULT_CONFIG));
			} catch (ConfigurationException e) {
				log.error("Could not load default configuration from "
						+ servletContext.getServletContext().getRealPath(
								DEFAULT_CONFIG));
				// e.printStackTrace();
			}
			if (OVERRIDE_CONFIG != null) {
				PropertiesConfiguration overrides = null;
				try {
					overrides = new PropertiesConfiguration();
					overrides.setFile(new File(OVERRIDE_CONFIG));
					overrides
							.setReloadingStrategy(new FileChangedReloadingStrategy());
					overrides.load();
				} catch (ConfigurationException e) {
					log.info("Could not load overrides", e);
				}
				compositeConfiguration.addConfiguration(overrides);
			}
			// Afterwards, add defaults so they're read second.
			compositeConfiguration.addConfiguration(defaults);
		}
	}

}
