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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class PersistentJobStoreDAO {

	protected static SqlJetDb db = null;

	private static final String TABLE_NAME = "jobstore";

	private static final String JOB_STORE_CREATE_SQL = "CREATE TABLE "
			+ TABLE_NAME + " ( " + " id INTEGER NOT NULL PRIMARY KEY"
			+ " , device TEXT NOT NULL " + " , status TEXT NOT NULL "
			+ " , displayText TEXT " + " , sigraw BLOB " + " , sigimage BLOB "
			+ " ) ; ";

	public static void create(String fileName) throws SqlJetException {
		File dbFile = new File(fileName);
		dbFile.delete();

		db = SqlJetDb.open(dbFile, true);
		db.getOptions().setAutovacuum(true);
		db.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
			db.getOptions().setUserVersion(1);
		} finally {
			db.commit();
		}

		db.beginTransaction(SqlJetTransactionMode.WRITE);
		try {
			db.createTable(JOB_STORE_CREATE_SQL);
		} finally {
			db.commit();
		}
	}

	public static void delete(Integer id) throws SqlJetException {
		synchronized (db) {
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				ISqlJetTable table = db.getTable(TABLE_NAME);
				ISqlJetCursor deleteCursor = table.scope(table
						.getPrimaryKeyIndexName(), new Object[] { id },
						new Object[] { id });
				while (!deleteCursor.eof()) {
					deleteCursor.delete();
				}
				deleteCursor.close();
			} finally {
				db.commit();
			}
		}
	}

	public static JobStoreItem get(Integer id) throws SqlJetException {
		synchronized (db) {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			try {
				ISqlJetTable table = db.getTable(TABLE_NAME);
				ISqlJetCursor cursor = table.lookup("id", id);

				JobStoreItem item = new JobStoreItem();
				item.setId(id);
				item.setDevice(cursor.getString("device"));
				item.setStatus(cursor.getString("status"));
				item.setDisplayText(cursor.getString("displayText"));
				item.setSignatureRaw(cursor.getBlobAsArray("sigraw"));
				item.setSignatureImage(cursor.getBlobAsArray("sigimage"));

				cursor.close();

				return item;
			} finally {
				db.commit();
			}
		}
	}

	public static List<JobStoreItem> unassignedJobs() throws SqlJetException {
		synchronized (db) {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			try {
				ISqlJetTable table = db.getTable(TABLE_NAME);
				ISqlJetCursor cursor = table.lookup("status",
						JobStoreItem.STATUS_NEW);

				List<JobStoreItem> items = new ArrayList<JobStoreItem>();

				do {
					JobStoreItem item = new JobStoreItem();
					item.setId((int) cursor.getInteger("id"));
					item.setDevice(cursor.getString("device"));
					item.setStatus(cursor.getString("status"));
					item.setDisplayText(cursor.getString("displayText"));
					item.setSignatureRaw(cursor.getBlobAsArray("sigraw"));
					item.setSignatureImage(cursor.getBlobAsArray("sigimage"));
					items.add(item);
				} while (cursor.next());

				cursor.close();

				return items;
			} finally {
				db.commit();
			}
		}
	}

	public static int insert(JobStoreItem i) throws SqlJetException {
		synchronized (db) {
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				ISqlJetTable table = db.getTable(TABLE_NAME);
				return (int) table.insert(i.getId(), i.getDevice(), i
						.getStatus(), i.getDisplayText(), i.getSignatureRaw(),
						i.getSignatureImage());
			} finally {
				db.commit();
			}
		}
	}

	public static void update(JobStoreItem i) throws SqlJetException {
		synchronized (db) {
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			try {
				ISqlJetTable table = db.getTable(TABLE_NAME);
				ISqlJetCursor updateCursor = table.scope(table
						.getPrimaryKeyIndexName(), new Object[] { i.getId() },
						new Object[] { i.getId() });
				do {
					updateCursor.update(i.getId(), i.getDevice(),
							i.getStatus(), i.getDisplayText(), i
									.getSignatureRaw(), i.getSignatureImage());
				} while (updateCursor.next());
				updateCursor.close();
			} finally {
				db.commit();
			}
		}
	}

}
