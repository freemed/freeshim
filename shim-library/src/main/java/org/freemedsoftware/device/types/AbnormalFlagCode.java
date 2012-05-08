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

public enum AbnormalFlagCode {

	BELOW_LOW_NORMAL("L"), ABOVE_HIGH_NORMAL("H"), BELOW_LOWER_PANIC_LIMIT("LL"), ABOVE_UPPER_PANIC_LIMIT(
			"HH"), BELOW_INSTRUMENT_SCALE("<"), ABOVE_INSTRUMENT_SCALE(">"), NORMAL(
			"N"), ABNORMAL("A"), VERY_ABNORMAL("AA"), NO_RANGE_DEFINED(null), SIGNIFICANT_CHANGE_UP(
			"U"), SIGNIFICANT_CHANGE_DOWN("D"), BETTER("B"), WORSE("W"), SUCEPTIBLE(
			"S"), RESISTANT("R"), INTERMEDIATE("I"), MODERATELY_SUCEPTIBLE("MS"), VERY_SUCEPTIBLE(
			"VS");

	private String txt = null;

	private AbnormalFlagCode(String txt) {
		this.txt = txt;
	}

	@Override
	public String toString() {
		return this.txt;
	}
}
