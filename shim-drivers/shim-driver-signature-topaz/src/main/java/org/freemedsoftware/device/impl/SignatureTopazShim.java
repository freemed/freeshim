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

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.beans.Beans;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.freemedsoftware.device.DeviceCapability;
import org.freemedsoftware.device.JobStoreItem;
import org.freemedsoftware.device.PersistentJobStoreDAO;
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

	protected Integer currentJobId = null;

	protected JobStoreItem job = null;

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

	/**
	 * Number of milliseconds to display signature confirmation message.
	 */
	protected final static int DISPLAY_SIGNATURE_CONFIRMATION = 7000;

	protected Map<String, Object> config = new HashMap<String, Object>();

	protected static Timer timer = new Timer();

	protected static AtomicLong lastSeen = new AtomicLong();

	protected static long requestTime = 0L;

	protected static int numSigPoints = 0;

	protected static String LCD_FONT_TYPEFACE = Font.SERIF;

	protected static String SIGNATURE_CONFIRMATION_MESSAGE = "Your signature has been recorded.";

	protected static String SIGNATURE_NONE_MESSAGE = "No signature has been given!";

	protected static Font tabletFont = Font.getFont(LCD_FONT_TYPEFACE);

	protected class LcdWriteDestination {
		/**
		 * 0 = Foreground
		 */
		public final static int FOREGROUND = 0;
		/**
		 * 1 = Background memory in tablet
		 */
		public final static int BACKGROUND_MEMORY = 1;
	}

	protected class LcdCaptureMode {
		/**
		 * Mode 0 no LCD commands are sent to the tablet
		 */
		public final static int NO_LCD = 0;
		/**
		 * Mode 1 - sets capture mode to be active with Autoerase in the tablet
		 */
		public final static int ENABLED_AUTOERASE = 1;
		/**
		 * Mode 2 - sets the tablet to persistent ink capture without autoerase
		 */
		public final static int ENABLED = 2;
		/**
		 * Mode 3 - signature ink is displayed inverted on a suitable dark
		 * background set using the Graphic functions.
		 */
		public final static int ENABLED_INVERTED = 3;
	}

	protected class LcdWriteMode {
		/**
		 * Mode 0 - Clear: The Display is cleared at the specified location.
		 */
		public final static int CLEAR = 0;
		/**
		 * Mode 1 - Complement: The Display is complemented at the specified
		 * location.
		 */
		public final static int COMPLEMENT = 1;
		/**
		 * Mode 2 - WriteOpaque: The contents of the background memory in the
		 * tablet are transferred to the LCD display, overwriting the contents
		 * of the LCD display.
		 */
		public final static int WRITE_OPAQUE = 2;
		/**
		 * Mode 3 - WriteTransparent: The contents of the background memory in
		 * the tablet are combined with and transferred to the visible LCD
		 * memory
		 */
		public final static int WRITE_TRANSPARENT = 3;
	}

	protected class TopazTimerSignatureClearDisplayTask extends TimerTask {
		@Override
		public void run() {
			// Only do this if there's no current job id running
			if (currentJobId == null) {
				log.info("Clearing display.");
				clearTabletLCD();
				// sigObj.lcdRefresh(0, 0, 0, sigObj.getTabletLCDXSize(),
				// sigObj.getTabletLCDYSize());
				// sigObj.setLCDCaptureMode(LcdCaptureMode.NO_LCD);
				// sigObj.setEnabled(false);
				// sigObj.setTabletState(0);
				// sigObj.clearTablet();
			} else {
				log.warn("Attempted to clear LCD but job id of " + currentJobId
						+ " is present");
			}
		}
	}

	protected class TopazTimerTask extends TimerTask {

		@Override
		public void run() {
			log.info("TopazTimerTask running");

			int sigPoints = 0;
			try {
				sigPoints = sigObj.getSignatureData().m_PointArray.length;
			} catch (Exception ex) {
				log.warn("Could not fetch signature data point length"
						+ ", dropping back into loop", ex);
				return;
			}

			// If we have no points at all ...
			if (sigPoints == 0) {
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

					// Cancel the job.
					cancelJob();

					// Clear everything
					log.info("Clearing signature job id");
					currentJobId = null;
					job = null;
					numSigPoints = 0;
					lastSeen.set(0L);

					log.info("Clearing tablet.");
					clearTabletLCD();
					// sigObj.setEnabled(false);
					// sigObj.setTabletState(0);
					// sigObj.clearTablet();

					log.info("Cancelling timer.");
					cancel();

					writeTabletMessage(0, 0, SIGNATURE_NONE_MESSAGE);

					// Clear the LCD after certain number of ms.
					timer.schedule(new TopazTimerSignatureClearDisplayTask(),
							DISPLAY_SIGNATURE_CONFIRMATION);
				}
			}

			log.debug("numSigPoints = " + numSigPoints
					+ " / sigObj.getSignatureData().m_PointArray.length = "
					+ sigPoints);
			if (sigPoints > numSigPoints) {
				// Bump up when an event was last seen to now.
				lastSeen.set(System.currentTimeMillis());

				// ... and increase the counter
				numSigPoints = sigPoints;

				// Wait until next task execution
				log
						.debug("New points seen since last polling, dropping back into loop.");
				return;
			}

			if ((System.currentTimeMillis() - lastSeen.get()) < SIGNATURE_MAXIMUM_WAIT) {
				log
						.debug("Dropping back into loop, need to wait a little longer.");
				return;
			}

			// Test to see if anything is there
			if (sigPoints < SIGNATURE_MINIMUM_POINTS) {
				// Wait until next task execution
				log.debug("Not enough points recorded, back to waiting");
				return;
			}

			// If everything else works properly, we're going to deal with the
			// data we've got.

			// "Handle" the signature
			log.info("Attempting to handle signature");
			handleSignature();

			// Clear everything
			log.info("Clearing signature job id");
			job = null;
			currentJobId = null;
			numSigPoints = 0;
			lastSeen.set(0L);
			log.info("Clearing pad.");
			clearTabletLCD();
			// sigObj.setEnabled(false);
			// sigObj.setTabletState(0);
			// sigObj.clearTablet();

			// Just cancel timer task, otherwise the <Timer> object isn't
			// reusable.
			log.info("Cancelling timer task.");
			cancel();

			writeTabletMessage(0, 0, SIGNATURE_CONFIRMATION_MESSAGE);

			// Clear the LCD after certain number of ms.
			timer.schedule(new TopazTimerSignatureClearDisplayTask(),
					DISPLAY_SIGNATURE_CONFIRMATION);
		}
	}

	@Override
	public void configure(HashMap<String, Object> config) {
		log.info("Loading configuration");
		this.config = config;
	}

	@Override
	public List<String> getConfigurationOptions() {
		return Arrays.asList(new String[] { "topaz.tabletModel",
				"topaz.tabletPort" });
	}

	@Override
	public boolean isProcessing() {
		return (currentJobId != null);
	}

	@Override
	public void init() throws Exception {
		// Load Topaz SigPlus driver on top of rxtx or HID driver
		log.debug("init()");
		ClassLoader cl = (SigPlus.class).getClassLoader();
		log.debug("Instantiating Topaz SigPlus driver");
		sigObj = (SigPlus) Beans.instantiate(cl, "com.topaz.sigplus.SigPlus");

		// Clear all tablet configuration
		sigObj.clearTablet();

		// Pull from actual configuration
		try {
			sigObj.setTabletModel((String) config.get("topaz.tabletModel"));
			sigObj.setTabletComPort((String) config.get("topaz.tabletPort"));
		} catch (Exception ex) {
			log.error(ex);
			throw ex;
		}

		// Attach event listener
		sigObj.addSigPlusListener(this);

		// By default, disable this object until it has to become active
		sigObj.setEnabled(false);
	}

	@Override
	public void close() throws Exception {
		if (timer != null) {
			log.info("Cancelling timer");
			timer.cancel();
		}
		if (sigObj != null) {
			clearTabletLCD();
			// sigObj.setEnabled(false);
			// sigObj.setTabletState(0);
			// sigObj.clearTablet();
		}
	}

	@Override
	public boolean initJobRequest(JobStoreItem item) throws Exception {
		// If we're already processing a request, do not progress any further.
		if (currentJobId != null) {
			log.error("Job in progress already for pad.");
			return false;
		} else {
			if (item == null) {
				job = new JobStoreItem();
				job.setId(0);
				job.setDisplayText("Patient: Rufus T Firefly");
				job.setStatus("PENDING");
			} else {
				log.info("Setting current jobId to " + item.getId());
				job = item;
			}
		}

		// Record the request time
		requestTime = System.currentTimeMillis();

		// Clear tablet
		sigObj.clearTablet();

		// Store uid locally
		currentJobId = job.getId();

		// Reset counters
		numSigPoints = 0;
		lastSeen.set(0L);

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
			log.info("X size = " + sigObj.getTabletLCDXSize() + ", Y size = "
					+ sigObj.getTabletLCDYSize());
			sigObj.lcdRefresh(0, 0, 0, sigObj.getTabletLCDXSize(), sigObj
					.getTabletLCDYSize());
			sigObj.setLCDCaptureMode(LcdCaptureMode.ENABLED);
			sigObj.lcdWriteString(LcdWriteDestination.FOREGROUND,
					LcdWriteMode.WRITE_OPAQUE, 0, 0, "Please sign below",
					tabletFont);
			sigObj.lcdWriteString(LcdWriteDestination.FOREGROUND,
					LcdWriteMode.WRITE_OPAQUE, 0,
					sigObj.getTabletLCDYSize() - 20, job.getDisplayText(),
					tabletFont);
		} catch (Exception ex) {
			log.info("LCD adjustment failed with " + ex.toString());
		}

		// Schedule a timer task to attempt to poll the pad
		lastSeen.set(System.currentTimeMillis());
		timer.schedule(new TopazTimerTask(), SIGNATURE_WAIT_DURATION,
				SIGNATURE_WAIT_DURATION);

		return true;
	}

	protected void clearTabletLCD() {
		if (sigObj != null) {
			sigObj.lcdRefresh(0, 0, 0, sigObj.getTabletLCDXSize(), sigObj
					.getTabletLCDYSize());
			sigObj.setLCDCaptureMode(LcdCaptureMode.NO_LCD);
			sigObj.setEnabled(false);
			sigObj.setTabletState(0);
			sigObj.clearTablet();
		}
	}

	protected void writeTabletMessage(int x, int y, String message) {
		// Attempt to display confirmation message
		try {
			log.info("Attempting to display message.");
			sigObj.setTabletState(1);

			sigObj.setLCDCaptureMode(LcdCaptureMode.ENABLED);
			sigObj.lcdWriteString(LcdWriteDestination.FOREGROUND,
					LcdWriteMode.WRITE_OPAQUE, x, y, message, tabletFont);
		} catch (Throwable t) {
			log.warn("Failed to properly write message", t);
			sigObj.setTabletState(0);
		}
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
			sigObj.setLCDCaptureMode(LcdCaptureMode.ENABLED_AUTOERASE);
		} catch (Exception ex) {
			log.info("LCD adjustment failed with " + ex.toString());
		}

		TopazSigCapData sigData = sigObj.getSignatureData();
		log.debug(sigData.m_byRawData);
		job.setSignatureRaw(sigData.m_byRawData.getBytes());

		// Store data in object
		log.info("Storing data.");

		// Create PNG image
		try {
			BufferedImage sigImage = sigObj.sigImage();
			int w = sigImage.getWidth(null);
			int h = sigImage.getHeight(null);
			log.info("getWidth/getHeight returned : w = " + w + ", h = " + h);
			try {
				w = sigObj.getTabletLCDXSize();
				h = sigObj.getTabletLCDYSize();
				log.info("Used LCD to get dimensions of w = " + w + ", h = "
						+ h);
			} catch (Exception ex) {
				log.warn(ex);
			}

			sigObj.setImageJustifyMode(5);
			sigObj.setImagePenWidth(1);
			sigObj.setImageXSize(w);
			sigObj.setImageYSize(h);

			// Redundant.
			sigImage = sigObj.sigImage();

			int[] pixels = new int[((w * h) * 2) + 1];
			log.debug("pixel array length = " + pixels.length);
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
			log.debug("Populating signature image in job item.");
			job.setSignatureImage(fos.toByteArray());
		} catch (Throwable t) {
			log.error(t);
		}

		// Disable tablet capture mode
		sigObj.setTabletState(0);

		log.info("Recording job status");
		job.setStatus(JobStoreItem.STATUS_COMPLETED);
		if (job.getId() != 0) {
			log.info("Writing to persistent job store");
			try {
				PersistentJobStoreDAO.update(job);
			} catch (Exception e) {
				log.error("Failed to write", e);
			}
		} else {
			log
					.info("Skipping persistent job store write, as this is a test case.");
			log.debug("length = " + job.getSignatureImage().length
					+ ", data = " + new String(job.getSignatureImage()));
		}
	}

	public void handleTabletTimerEvent(SigPlusEvent0 event) {
	}

	public void cancelJob() {
		log.info("Recording error job status");
		job.setStatus(JobStoreItem.STATUS_ERROR);
		if (job.getId() != 0) {
			log.info("Writing to persistent job store");
			try {
				PersistentJobStoreDAO.update(job);
			} catch (Exception e) {
				log.error("Failed to write", e);
			}
		} else {
			log
					.info("Skipping persistent job store write, as this is a test case.");
		}
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
		HashMap<String, Object> c = new HashMap<String, Object>();
		c.put("topaz.tabletModel", "SignatureGemLCD1X5");
		c.put("topaz.tabletPort", "HID1"); // HID1 == "HSB" tablet HID port
		s.configure(c);
		System.out.println("Initializing shim driver");
		s.init();
		System.out.println("Initializing signature request for pad");
		s.initJobRequest(null);
	}

}
