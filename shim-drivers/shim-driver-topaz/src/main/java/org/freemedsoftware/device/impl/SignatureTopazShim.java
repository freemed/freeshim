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

import gnu.io.CommDriver;

import java.beans.Beans;
import java.util.HashMap;

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

	@Override
	public void configure(HashMap<String, Object> config) {
	}

	@Override
	public void init() throws Exception {
		// Initialize rxtx communications driver
		String drivername = "gnu.io.CommDriver";
		CommDriver driver = (CommDriver) Class.forName(drivername)
				.newInstance();
		driver.initialize();

		// Load Topaz SigPlus driver on top of rxtx
		ClassLoader cl = (SigPlus.class).getClassLoader();
		sigObj = (SigPlus) Beans.instantiate(cl, "com.topaz.sigplus.SigPlus");

		// Clear all tablet configuration
		sigObj.clearTablet();

		// TODO: FIXME: Need to pull from actual configuration
		sigObj.setTabletModel("SignatureGem1X5");
		sigObj.setTabletComPort("COM1");

		// Attach event listener
		sigObj.addSigPlusListener(this);

		// By default, disable this object until it has to become active
		sigObj.setEnabled(false);
	}

	@Override
	public void close() throws Exception {
		if (sigObj != null) {
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

		// Clear tablet
		sigObj.clearTablet();

		// Store uid locally
		currentJobId = uid;

		sigObj.autoKeyStart();
		sigObj.setAutoKeyData("Sample Encryption Data");
		sigObj.autoKeyFinish();
		sigObj.setEncryptionMode(2);
		sigObj.setSigCompressionMode(1);

		// Attempt to readjust LCD on models which support it
		try {
			sigObj.lcdRefresh(0, 0, 0, 640, 480);
			sigObj.setLCDCaptureMode(2);
			// sigObj.lcdWriteString(...);
		} catch (Exception ex) {
			log.info("LCD adjustment failed with " + ex.toString());
		}

		return true;
	}

	public void handleKeyPadData(SigPlusEvent0 event) {
	}

	public void handleNewTabletData(SigPlusEvent0 event) {
		if (currentJobId == null) {
			log.error("No current job id, unexpected data");
		}

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
		// TODO: store data
	}

	public void handleTabletTimerEvent(SigPlusEvent0 event) {
	}

}
