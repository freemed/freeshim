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
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.freemedsoftware.device.impl;

import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.LabSerialInterface;
import org.freemedsoftware.device.ShimDevice;

@ShimDevice(name = "Clinitek50 Lab Shim", capability = DeviceCapability.DEVICE_VITAL_SIGNS)
public class LabClinitek50Shim extends LabSerialInterface {

	protected Logger log = Logger.getLogger(LabClinitek50Shim.class);

	protected Timer timer = new Timer();

	protected Integer jobId = null;

	protected JobStoreItem item = null;

	protected class ClinitekPacket {
		protected boolean success;
		protected String data;

		public ClinitekPacket(boolean success, String data) {
			setSuccess(success);
			setData(data);
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public boolean getSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}
	}

	// Constant declarations
	public static String ETX = "\003";
	public static String ENQ = "\005";
	public static String CMD_REQ_ID = ENQ;
	public static String ACK = "\006";
	public static String CMD_ACK_PACKET = ACK;
	public static String XON = "\021";
	public static String DC1 = XON;
	public static String DC2 = "\022";
	// this is contrary to the specs which show DC2 and DC3 swapped around
	public static String CMD_REQ_DATA = DC2;
	public static String XOFF = "\023";
	public static String DC3 = XOFF;
	public static String NAK = "\025";
	public static String CMD_NAK_PACKET = NAK;
	public static String EOL = "\r\n";
	public static String CRLF = EOL;
	public static int TIMEOUT = 3000;
	public static int MAX_PACKET_SIZE = 1024;
	public static String PACKET_HEADER = "\002\r\n"; // STX CR LF
	public static String DEV_ID = "6510";
	public static String[] KNOWN_GOOD_REVS = new String[] { "  ", "A " };
	public static String[] KNOWN_GOOD_SW_VERS = new String[] { "01.00", "01.02" };
	public static String[] KNOWN_STIX_TYPES = new String[] { "MULTISTIX 10 SG" };
	public static String DATE_FORMAT = "yyMMdd";
	public static String TIME_FORMAT = "HHmm";
	public static DateFormat timeFormat = new SimpleDateFormat(DATE_FORMAT
			+ TIME_FORMAT);

	public LabClinitek50Shim() {
		setConfigName("org.freemedsoftware.device.impl.LabClinitek50Shim");
	}

	@Override
	public void init() throws Exception {
		// Run original init
		super.init();

		// DTR must be LOW according to specs,
		// if physically connected and set to HIGH the
		// device controller will enter programming mode
		serialInterface.getSerialPort().setDTR(false);
	}

	protected String getRecord() {
		// enable data transfer
		try {
			serialInterface.write(XON);
		} catch (IOException e) {
			log.error(e);
		}

		// request data
		try {
			serialInterface.write(CMD_REQ_DATA);
		} catch (IOException e) {
			log.error(e);
		}
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			log.error(e);
			return null;
		}

		// receive data
		String packet = receivePacket();
		if (packet == null) {
			return null;
		}
		// if packet == -1:
		// return -1
		if (!verifyDataPacket(packet)) {
			// NAK packet
			try {
				serialInterface.write(CMD_NAK_PACKET);
			} catch (IOException e) {
				log.error(e);
			}
			return null;
		}
		try {
			serialInterface.write(CMD_ACK_PACKET);
		} catch (IOException e) {
			log.error(e);
		}
		return packet;
	}

	protected String receivePacket() {
		// wait for ETX which is supposed to terminate all packets
		ClinitekPacket packet = waitForStr(ETX, TIMEOUT, MAX_PACKET_SIZE);
		if (!packet.getSuccess()) {
			if (packet.getData().equals(NAK)) {
				log.info("no more data in device");
				return "";
			} else {
				log.error("receiving packet from device failed");
				return null;
			}
		}
		if (!verifyGenericPacketStructure(packet.getData())) {
			// NAK packet
			try {
				serialInterface.write(CMD_NAK_PACKET);
			} catch (IOException e) {
				log.error(e);
			}
			return null;
		}
		return packet.getData();
	}

	protected boolean verifyGenericPacketStructure(String packet) {
		// does it start with STX CR LF ?
		if (packet.substring(0, 2) != PACKET_HEADER) {
			log.error("packet does not start with STX CR LF");
			log.debug(packet);
			return false;
		}
		// does it have at least 5 lines ?
		if (!(packet.split(EOL).length > 2)) {
			log.error("packet does not have at least 5 lines");
			log.debug(packet);
			return false;
		}
		// does it have a valid checksum ?
		String rxd_crc = "0x"
				+ packet.substring(packet.length() - 4, packet.length() - 2)
						.toLowerCase();
		// TODO: check crc
		// crc_val = reduce(lambda x, y: x + ord(y), tuple(packet[1:-3]), 0) &
		// 255;
		String crc_val = "";
		String hex_str = ""; // hex(crc_val)[2:];
		String calced_crc = "";
		if (hex_str.length() == 1) {
			calced_crc = "0x0" + hex_str;
		} else {
			calced_crc = "0x" + hex_str;
			if (!calced_crc.equals(rxd_crc)) {
				log.error("packet checksum error: received [" + rxd_crc
						+ "] vs. calculated [" + calced_crc + "]");
				log.debug(packet);
				return false;
			}
		}
		// seems valid
		log.debug("generic packet structure is valid");
		return true;
	}

	protected boolean verifyDetectPacket(String packet) {
		String[] lines = packet.split(EOL);
		// product ID: 6510 = Clinitek 50
		String tmp = lines[0].substring(0, 3);
		if (tmp != DEV_ID) {
			log
					.error("device does not seem to be a Clinitek 50, product ID is ["
							+ tmp + "], expected [" + DEV_ID + "]");
			for (String line : lines) {
				log.debug(line);
			}
			return false;
		}
		// product revision
		tmp = lines[1].substring(3, 5);
		if (!Arrays.asList(KNOWN_GOOD_REVS).contains(tmp)) {
			log.warn("product revision [" + tmp
					+ "] untested, trying to continue anyways");
		}
		// software version
		tmp = lines[1].substring(5, 10);
		if (!Arrays.asList(KNOWN_GOOD_SW_VERS).contains(tmp)) {
			log.warn("software version [" + tmp
					+ "] untested, trying to continue anyways");
		}
		// date/time
		Date nowTimestamp = new Date();
		Date timestamp = null;
		try {
			timestamp = timeFormat.parse(lines[0].substring(11, 21));
		} catch (ParseException e) {
			log.error(e);
		}
		int age = 0;
		if (timestamp != null) {
			log.info("device timestamp: " + timestamp.toString());
			log.info("system timestamp: " + new Date().toString());
			long msAge = (nowTimestamp.getTime() - timestamp.getTime());
			if (msAge != 0) {
				age = (int) (msAge / (1000 * 60 * 60));
			} else {
				age = 0;
			}
		}

		if (age > 6) {
			log.error("device time is off by " + age
					+ " hours, please correct that");
			return false;
		}

		// language-unit profile
		String[] languageUnit = lines[1].split(" - ");
		String lang = languageUnit[0];
		String units = languageUnit[1];
		log.info("language: " + lang);
		log.info("unit system: " + units);
		// STIX type
		String stix_type = lines[2].trim();
		if (!Arrays.asList(KNOWN_STIX_TYPES).contains(stix_type)) {
			log.error("don't know how to handle stix of type " + stix_type);
			return false;
		}
		// seems valid
		return true;
	}

	protected boolean verifyDataPacket(String packet) {
		log.info("skipping verification of data packet");
		// seems valid
		return true;
	}

	protected ClinitekPacket waitForStr(String aString, int aTimeout,
			int max_bytes) {
		try {
			serialInterface.getSerialPort().enableReceiveTimeout(aTimeout);
		} catch (UnsupportedCommOperationException e) {
			log.error(e);
		}

		if (aString == null) {
			return new ClinitekPacket(true, null);
		}
		if (aString == "") {
			return new ClinitekPacket(false, "");
		}

		if (max_bytes < aString.length()) {
			max_bytes = aString.length() + 1;
		}

		String rxd = "";
		int loop = 0;
		int slice = 100;
		// how many loops ?
		int max_loops = Math.abs(aTimeout / slice);
		// wait for data
		while (loop < max_loops) {
			loop += 1;
			// something there
			int ch = -1;
			try {
				ch = serialInterface.getInputStream().read();
			} catch (IOException e) {
				log.error(e);
				return new ClinitekPacket(false, rxd);
			}
			if (ch != -1) {
				// get all there is
				while (ch != -1) {
					rxd = rxd + ch;
					// did this contain our expected string already ?
					if (rxd.contains(aString)) {
						return new ClinitekPacket(true, rxd);
					}

					// did we exceed our character buffer limit ?
					// this stops runaway serial ports
					if (rxd.length() >= max_bytes) {
						log.error("exceeded maximum # of bytes (" + max_bytes
								+ ") to receive");
						return new ClinitekPacket(false, rxd);
					}
				}
				// nothing there, wait a slice
			} else {
				if (rxd.length() >= max_bytes) {
					log.error("exceeded maximum # of bytes to receive");
					return new ClinitekPacket(false, rxd);
				}
				try {
					Thread.sleep(slice);
				} catch (InterruptedException e) {
					log.error(e);
					return new ClinitekPacket(false, rxd);
				}
			}
		}

		// hm, waited for aTimeout but expected string not received
		log.warn("wait for [" + aString + "] timed out after " + aTimeout
				+ " ms");
		log.debug(rxd);
		return new ClinitekPacket(false, rxd);
	}
}
