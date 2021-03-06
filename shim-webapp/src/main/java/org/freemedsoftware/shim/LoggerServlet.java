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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Servlet implementation class LoggerServlet
 */
public class LoggerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	static final Logger logger = Logger.getLogger(LoggerServlet.class);

	/**
	 * Default constructor.
	 */
	public LoggerServlet() {
		System.out.println(getClass().getCanonicalName() + " initializing");
	}

	@Override
	public void init() throws ServletException {
		System.out.println("LogggerServlet init() starting.");
		String log4jfile = getInitParameter("log4j-properties");
		System.out.println("log4j-properties: " + log4jfile);
		if (log4jfile != null) {
			String propertiesFilename = getServletContext().getRealPath(
					log4jfile);
			System.out.println("Using file " + propertiesFilename);
			PropertyConfigurator.configure(propertiesFilename);
			logger.info("logger configured.");
		} else {
			String propertiesFilename = getServletContext().getRealPath(
					"/WEB-INF/log4j.properties");
			System.out.println("Using file " + propertiesFilename);
			PropertyConfigurator.configure(propertiesFilename);
			logger.info("logger configured.");
		}
		System.out.println("LoggerServlet init() done.");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

}
