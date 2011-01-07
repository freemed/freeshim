/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management System
 * Copyright (C) 1999-2011 FreeMED Software Foundation
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

import java.util.HashMap;
import java.util.List;

public interface DeviceInterface {

	public void configure(HashMap<String, Object> config);

	public List<String> getConfigurationOptions();

	public void init() throws Exception;

	public void close() throws Exception;

	public boolean isProcessing();

	/**
	 * Initialize request for a job on a device.
	 * 
	 * @param item
	 *            <JobStoreItem> object for this job.
	 * @return Success
	 * @throws Exception
	 */
	public boolean initJobRequest(JobStoreItem item) throws Exception;

}
