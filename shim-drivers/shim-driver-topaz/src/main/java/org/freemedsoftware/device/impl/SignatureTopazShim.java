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

package org.freemedsoftware.device.impl;

import java.awt.image.BufferedImage;
import java.beans.Beans;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.ShimDevice;
import org.freemedsoftware.device.SignatureInterface;

import com.topaz.sigplus.SigPlus;
import com.topaz.sigplus.SigPlusEvent0;
import com.topaz.sigplus.SigPlusListener;
import com.topaz.sigplus.TopazSigCapData;

@ShimDevice(name = "Topaz Signature Shim", capability = DeviceCapability.DEVICE_SIGNATURE_TABLET)
public class SignatureTopazShim implements SignatureInterface, SigPlusListener {

	protected Logger log = Logger.getLogger(SignatureTopazShim.class);

	protected SigPlus sigObj = null;

	protected String currentJobId = null;

	/**
	 * Amount of time in milliseconds to wait between signature polling.
	 */
	protected final static int SIGNATURE_WAIT_DURATION = 500;

	/**
	 * Maximum amount of time to wait after last signature point recorded.
	 */
	protected final static int SIGNATURE_MAXIMUM_WAIT = 2000;

	/**
	 * Wait this much time in milliseconds before giving up.
	 */
	protected final static int SIGNATURE_INITIAL_WAIT = 15000;

	protected final static int SIGNATURE_MINIMUM_POINTS = 10;

	protected static Timer timer = new Timer();

	protected static AtomicLong lastSeen = new AtomicLong();

	protected static long requestTime = 0L;

	protected static int numSigPoints = 0;

	protected class TopazTimerTask extends TimerTask {

		@Override
		public void run() {
			log.info("TopazTimerTask running");

			// If we have no points at all ...
			if (sigObj.getSignatureData().m_PointArray.length == 0) {
				// ... and we have been waiting for less than
				// SIGNATURE_INITIAL_WAIT ms
				if ((System.currentTimeMillis() - requestTime) < SIGNATURE_INITIAL_WAIT) {
					log.debug("No points seen yet, waiting for about "
							+ (SIGNATURE_INITIAL_WAIT - (System
									.currentTimeMillis() - requestTime))
							+ " ms before giving up");

					// Go back to waiting.
					return;
				} else {
					log.warn("Failed to see any points after "
							+ (System.currentTimeMillis() - requestTime)
							+ " ms, aborting.");

					// Clear everything
					log.info("Clearing signature job id");
					currentJobId = null;
					lastSeen.set(0L);
					log.info("Clearing tablet.");
					sigObj.setEnabled(false);
					sigObj.setTabletState(0);
					sigObj.clearTablet();

					log.info("Cancelling timer.");
					timer.cancel();
				}
			}

			log.debug("numSigPoints = " + numSigPoints
					+ " / sigObj.getSignatureData().m_PointArray.length = "
					+ sigObj.getSignatureData().m_PointArray.length);
			if (sigObj.getSignatureData().m_PointArray.length > numSigPoints) {
				// Bump up when an event was last seen to now.
				lastSeen.set(System.currentTimeMillis());

				// ... and increase the counter
				numSigPoints = sigObj.getSignatureData().m_PointArray.length;

				// Wait until next task execution
				log
						.debug("New points seen since last polling, dropping back into loop.");
				return;
			}

			// Test to see if anything is there
			if (sigObj.getSignatureData().m_PointArray.length < SIGNATURE_MINIMUM_POINTS) {
				// Wait until next task execution
				log.debug("Not enough points recorded, back to waiting");
				return;
			}

			// If everything else works properly, we're going to deal with the
			// data we've got.

			// "Handle" the signature
			handleSignature();

			// Clear everything
			log.info("Clearing signature job id");
			currentJobId = null;
			lastSeen.set(0L);
			log.info("Clearing pad.");
			sigObj.setEnabled(false);
			sigObj.setTabletState(0);
			sigObj.clearTablet();

			log.info("Cancelling timer.");
			timer.cancel();
		}
	}

	@Override
	public void configure(HashMap<String, Object> config) {
	}

	@Override
	public void init() throws Exception {
		// Load Topaz SigPlus driver on top of rxtx
		ClassLoader cl = (SigPlus.class).getClassLoader();
		sigObj = (SigPlus) Beans.instantiate(cl, "com.topaz.sigplus.SigPlus");

		// Clear all tablet configuration
		sigObj.clearTablet();

		// TODO: FIXME: Need to pull from actual configuration
		sigObj.setTabletModel("SignatureGem4X5");
		sigObj.setTabletComPort("HID1"); // HID1 == "HSB" tablet HID port

		// Attach event listener
		sigObj.addSigPlusListener(this);

		// By default, disable this object until it has to become active
		sigObj.setEnabled(false);
	}

	@Override
	public void close() throws Exception {
		if (sigObj != null) {
			sigObj.setEnabled(false);
			sigObj.setTabletState(0);
			sigObj.clearTablet();
		}
	}

	@Override
	public boolean initSignatureRequest(String uid) throws Exception {
		// If we're already processing a request, do not progress any further.
		if (currentJobId != null) {
			log.error("Job in progress already for pad.");
			return false;
		}

		// Record the request time
		requestTime = System.currentTimeMillis();

		// Clear tablet
		sigObj.clearTablet();

		// Store uid locally
		currentJobId = uid;

		sigObj.setEnabled(true);
		sigObj.autoKeyStart();
		sigObj.setAutoKeyData("Sample Encryption Data");
		sigObj.autoKeyFinish();
		sigObj.setEncryptionMode(2);
		sigObj.setSavePressureData(true);
		sigObj.setSigCompressionMode(1);

		// Enable "capture" mode
		sigObj.setTabletState(1);

		// Attempt to readjust LCD on models which support it
		try {
			sigObj.lcdRefresh(0, 0, 0, 640, 480);
			sigObj.setLCDCaptureMode(2);
			// sigObj.lcdWriteString(...);
		} catch (Exception ex) {
			log.info("LCD adjustment failed with " + ex.toString());
		}

		// Schedule a timer task to attempt to poll the pad
		lastSeen.set(System.currentTimeMillis());
		timer.schedule(new TopazTimerTask(), SIGNATURE_WAIT_DURATION,
				SIGNATURE_WAIT_DURATION);

		return true;
	}

	public void handleKeyPadData(SigPlusEvent0 event) {
	}

	public void handleNewTabletData(SigPlusEvent0 event) {
		// Update the record of when an event was last seen.
		lastSeen.set(System.currentTimeMillis());

		if (currentJobId == null) {
			log.error("No current job id, unexpected data");
		}
	}

	protected void handleSignature() {
		// mySigString = sigObj.getSigString();
		sigObj.setSigCompressionMode(0);
		sigObj.setEncryptionMode(0);
		sigObj.setKeyString("0000000000000000");

		// Attempt to readjust LCD on models which support it
		try {
			sigObj.lcdRefresh(0, 0, 0, 640, 480);
			sigObj.setLCDCaptureMode(2);
		} catch (Exception ex) {
			log.info("LCD adjustment failed with " + ex.toString());
		}

		TopazSigCapData sigData = sigObj.getSignatureData();
		log.trace(sigData.m_byRawData);

		// TODO: store data
		log.info("Storing data.");

		// Create PNG image
		sigObj.setImageJustifyMode(5);
		sigObj.setImagePenWidth(10);
		sigObj.setImageXSize(1000);
		sigObj.setImageYSize(350);
		BufferedImage sigImage = sigObj.sigImage();
		int w = sigImage.getWidth(null);
		int h = sigImage.getHeight(null);
		int[] pixels = new int[(w * h) * 2];
		sigImage.setRGB(0, 0, 0, 0, pixels, 0, 0);
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		try {
			ImageIO.write(sigImage, "png", fos);
		} catch (IOException ex) {
			log.error(ex);
		}
		try {
			fos.close();
		} catch (IOException ex) {
			log.error(ex);
		}

		// Disable tablet capture mode
		sigObj.setTabletState(0);

	}

	public void handleTabletTimerEvent(SigPlusEvent0 event) {
	}

	/**
	 * Main method for testing only
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		SignatureTopazShim s = new SignatureTopazShim();
		System.out.println("Initializing shim driver");
		s.init();
		System.out.println("Initializing signature request for pad");
		s.initSignatureRequest("12345");
	}

}
