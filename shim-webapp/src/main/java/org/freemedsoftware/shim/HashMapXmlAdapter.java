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

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class HashMapXmlAdapter extends
		XmlAdapter<HashMapType, HashMap<String, String>> {

	@Override
	public HashMap<String, String> unmarshal(HashMapType arg0) throws Exception {
		HashMap<String, String> r = new HashMap<String, String>();
		for (HashMapEntryType iter : arg0.entry) {
			r.put(iter.key, iter.value);
		}
		return r;
	}

	@Override
	public HashMapType marshal(HashMap<String, String> arg0) throws Exception {
		HashMapType r = new HashMapType();
		r.entry = new ArrayList<HashMapEntryType>();
		for (String iter : arg0.keySet()) {
			HashMapEntryType e = new HashMapEntryType();
			e.key = iter;
			e.value = arg0.get(iter);
			r.entry.add(e);
		}
		return r;
	}

}
