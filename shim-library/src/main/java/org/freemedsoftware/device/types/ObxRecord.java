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

package org.freemedsoftware.device.types;

import java.io.Serializable;

public class ObxRecord implements Serializable {

	private static final long serialVersionUID = 773066854577636760L;

	private String observationIdentifier;
	private String observationValue;
	private String units;
	private String producerId;
	private String observationMethod;
	private AbnormalFlagCode abnormalFlag;

	public void setObservationIdentifier(String observationIdentifier) {
		this.observationIdentifier = observationIdentifier;
	}

	public String getObservationIdentifier() {
		return observationIdentifier;
	}

	public void setObservationValue(String observationValue) {
		this.observationValue = observationValue;
	}

	public String getObservationValue() {
		return observationValue;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getUnits() {
		return units;
	}

	public void setProducerId(String producerId) {
		this.producerId = producerId;
	}

	public String getProducerId() {
		return producerId;
	}

	public void setObservationMethod(String observationMethod) {
		this.observationMethod = observationMethod;
	}

	public String getObservationMethod() {
		return observationMethod;
	}

	public void setAbnormalFlag(AbnormalFlagCode abnormalFlag) {
		this.abnormalFlag = abnormalFlag;
	}

	public AbnormalFlagCode getAbnormalFlag() {
		return abnormalFlag;
	}

}
