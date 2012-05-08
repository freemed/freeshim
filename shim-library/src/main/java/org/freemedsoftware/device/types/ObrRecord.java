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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ObrRecord implements Serializable {

	private static final long serialVersionUID = 8699102427177717059L;

	private List<ObxRecord> observations = new ArrayList<ObxRecord>();
	private Date observationDate;
	private Date observationDateEnd;
	private SpecimenSourceCode specimenSource;

	public void addObservation(ObxRecord observation) {
		getObservations().add(observation);
	}

	public void setObservations(List<ObxRecord> observations) {
		this.observations = observations;
	}

	public List<ObxRecord> getObservations() {
		return observations;
	}

	public void setObservationDate(Date observationDate) {
		this.observationDate = observationDate;
	}

	public Date getObservationDate() {
		return observationDate;
	}

	public void setObservationDateEnd(Date observationDateEnd) {
		this.observationDateEnd = observationDateEnd;
	}

	public Date getObservationDateEnd() {
		return observationDateEnd;
	}

	public void setSpecimenSource(SpecimenSourceCode specimenSource) {
		this.specimenSource = specimenSource;
	}

	public SpecimenSourceCode getSpecimenSource() {
		return specimenSource;
	}

}
