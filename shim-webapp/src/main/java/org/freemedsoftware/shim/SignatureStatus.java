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

public enum SignatureStatus {
	PENDING("PENDING"), COMPLETE("COMPLETE"), ERROR("ERROR"), NEW("NEW");

	private String textual = null;

	SignatureStatus(String textual) {
		this.textual = textual;
	}

	public static SignatureStatus fromString(String textual) {
		if (textual.equalsIgnoreCase("PENDING")) {
			return SignatureStatus.PENDING;
		} else if (textual.equalsIgnoreCase("COMPLETE")) {
			return SignatureStatus.COMPLETE;
		} else if (textual.equalsIgnoreCase("ERROR")) {
			return SignatureStatus.ERROR;
		} else if (textual.equalsIgnoreCase("NEW")) {
			return SignatureStatus.NEW;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return this.textual;
	}
}
