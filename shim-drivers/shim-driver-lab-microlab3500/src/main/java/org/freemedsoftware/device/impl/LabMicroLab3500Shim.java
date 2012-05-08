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

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.LabSerialInterface;
import org.freemedsoftware.device.ShimDevice;

@ShimDevice(name = "MicroLab 3500 Lab Shim", capability = DeviceCapability.DEVICE_VITAL_SIGNS)
public class LabMicroLab3500Shim extends LabSerialInterface implements
		SerialPortEventListener {

	protected Logger log = Logger.getLogger(LabMicroLab3500Shim.class);

	protected static final int ACK = '\373'; // FB hex
	protected static final int NACK = '\004'; // 04 hex
	protected static final String PACKET_HEADER = "\245\132"; // A5 5A hex

	protected class TestResult {

		private Map<String, Double> testValues = new HashMap<String, Double>();
		private int testType;
		private String patientId;
		private Date testDate;

		public void setTestValues(Map<String, Double> testValues) {
			this.testValues = testValues;
		}

		public Map<String, Double> getTestValues() {
			return testValues;
		}

		public void addTestValue(String test, Double value) {
			getTestValues().put(test, value);
		}

		public void setTestType(int testType) {
			this.testType = testType;
		}

		public int getTestType() {
			return testType;
		}

		public void setPatientId(String patientId) {
			this.patientId = patientId;
		}

		public String getPatientId() {
			return patientId;
		}

		public void setTestDate(Date testDate) {
			this.testDate = testDate;
		}

		public Date getTestDate() {
			return testDate;
		}
	}

	/**
	 * Data packet encapsulation class, meant to hold a raw data packet.
	 */
	protected class DataPacket {

		private int packetType;
		private String data;

		public void setPacketType(int packetType) {
			this.packetType = packetType;
		}

		public int getPacketType() {
			return packetType;
		}

		public void setData(String data) {
			this.data = data;
		}

		public String getData() {
			return data;
		}
	}

	public LabMicroLab3500Shim() {
		setConfigName("org.freemedsoftware.device.impl.LabMicroLab3500Shim");
	}

	@Override
	public void init() throws Exception {
		super.init();

		// Force proper serial port config, since this is the only one that
		// works properly with this particular piece of hardware.
		serialInterface.getSerialPort().setSerialPortParams(4800,
				SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		serialInterface.getSerialPort().addEventListener(this);
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			log.info("SerialPortEvent.DATA_AVAILABLE");
			try {
				DataPacket packet = readDataPacket();
				handleDataPacket(packet);
			} catch (IOException e) {
				log.error("Failure, sending NACK", e);
				sendNack();
			}
		}
	}

	protected void handleDataPacket(DataPacket packet) {
		log.info("handleDataPacket for type " + packet.getPacketType());
		// TODO: handle different data packet types
	}

	/**
	 * Read length bytes of data from the serial port.
	 * 
	 * @param length
	 * @return
	 * @throws IOException
	 */
	protected String readBytes(int length) throws IOException {
		StringBuilder result = new StringBuilder();
		StringBuilder debugResult = new StringBuilder();
		for (int pos = 0; pos < length; pos++) {
			int ch = serialInterface.getInputStream().read();
			result.append((char) ch);
			debugResult.append(" ").append(charToHex((char) ch));
		}
		log.debug("Received " + length + "bytes (" + debugResult.toString()
				+ " )");
		return result.toString();
	}

	/**
	 * Send ACK packet to device.
	 * 
	 * @throws IOException
	 */
	protected void sendAck() throws IOException {
		serialInterface.getOutputStream().write(ACK);
	}

	/**
	 * Send NACK packet to device.
	 */
	protected void sendNack() {
		try {
			serialInterface.getOutputStream().write(NACK);
		} catch (IOException e) {
			log.error("Failed to send NACK", e);
		}
	}

	/**
	 * Read a <DataPacket> from the device.
	 * 
	 * @return
	 * @throws IOException
	 */
	protected DataPacket readDataPacket() throws IOException {
		// Read two bytes of data
		String ack = readBytes(2);
		if (!ack.contentEquals(PACKET_HEADER)) {
			throw new IOException("Invalid packet header");
		}
		// Content length is two bytes, n + 1
		String contentLengthString = readBytes(2);
		int contentLength = ((int) contentLengthString.charAt(0) * 256)
				+ ((int) contentLengthString.charAt(1)) - 1;

		// Packet type is one byte
		int packetType = serialInterface.getInputStream().read();

		String content = readBytes(contentLength);

		// TODO: check checksum
		int checksumByte = serialInterface.getInputStream().read();

		// Form data packet to send back
		DataPacket packet = new DataPacket();
		packet.setPacketType(packetType);
		packet.setData(content);

		// Send an acknowledgement if we've gotten this far without throwing an
		// exception.
		sendAck();

		return packet;
	}

	protected TestResult packetToTestResult(DataPacket packet) {
		String content = packet.getData();
		int year = (int) content.charAt(4) + 2000;
		int month = (int) content.charAt(3);
		int date = (int) content.charAt(2);
		int hourOfDay = (int) content.charAt(1);
		int minute = (int) content.charAt(0);
		int second = 0;

		Calendar testDate = GregorianCalendar.getInstance();
		testDate.set(year, month, date, hourOfDay, minute, second);

		TestResult result = new TestResult();
		result.setTestDate(testDate.getTime());
		// 5 2 FEV1 * 100
		result.addTestValue("FEV1", getDupleValue(content.charAt(5), content
				.charAt(6)) / 100);
		// 7 2 PEF * 100
		result.addTestValue("PEF", getDupleValue(content.charAt(7), content
				.charAt(8)) / 100);
		// 9 2 FVC * 100
		result.addTestValue("FVC", getDupleValue(content.charAt(9), content
				.charAt(10)) / 100);
		// 11 2 FER * 100
		result.addTestValue("FER", getDupleValue(content.charAt(11), content
				.charAt(12)) / 100);
		// 13 2 F50 * 100
		result.addTestValue("F50", getDupleValue(content.charAt(13), content
				.charAt(14)) / 100);
		// 15 2 F25 * 100
		result.addTestValue("F25", getDupleValue(content.charAt(15), content
				.charAt(16)) / 100);
		// 17 2 MEF * 100
		result.addTestValue("MEF", getDupleValue(content.charAt(17), content
				.charAt(18)) / 100);
		// 19 2 I50 * 100
		result.addTestValue("I50", getDupleValue(content.charAt(19), content
				.charAt(20)) / 100);
		// 21 2 R50 * 100
		result.addTestValue("R50", getDupleValue(content.charAt(21), content
				.charAt(22)) / 100);
		// 23 2 PIF * 100
		result.addTestValue("PIF", getDupleValue(content.charAt(23), content
				.charAt(24)) / 100);

		// Grab patient id
		result.setPatientId(content.substring(126, 126 + 9));

		return result;
	}

	protected double getDupleValue(char x, char y) {
		return (double) ((int) x << 8) + (int) y;
	}

	protected String byteToHex(byte b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	protected String charToHex(char c) {
		byte hi = (byte) (c >>> 8);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}
}
