package org.freemedsoftware.device;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ShimDeviceManagerTest extends TestCase {
	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public ShimDeviceManagerTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(ShimDeviceManagerTest.class);
	}

	public void testScanDeviceDrivers() {
		try {
			ShimDeviceManager<DeviceInterface> i = new ShimDeviceManager<DeviceInterface>();
			i.scanShimDevices();
			assertTrue(true);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
