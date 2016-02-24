package com.goodluck.autotest;

import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.DeviceBridge.ViewServerInfo;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		DeviceBridge.initDebugBridge("/Users/jungho/Library/Android/sdk/platform-tools/adb");
		int retryCnt = 5;
		do {
			Thread.sleep(500);
			System.out.println("Waiting for device...\n");
		} while (--retryCnt > 0 && DeviceBridge.getDevices().length == 0);
		
		if (DeviceBridge.getDevices().length == 0) {
			System.out.println("No device found!\n");
			return;
		}
		
		for (IDevice device : DeviceBridge.getDevices()) {
			System.out.println(device + "\n");
			DeviceBridge.setupDeviceForward(device);
		}
		
		IDevice device = DeviceBridge.getDevices()[0];
		if (!DeviceBridge.isViewServerRunning(device)) {
			System.out.println("startViewServer:" + DeviceBridge.startViewServer(device) + "\n");
		}
		
		ViewServerInfo server = DeviceBridge.loadViewServerInfo(device);
		System.out.println("protocol:" + server.protocolVersion + "\n");
		System.out.println("server:" + server.serverVersion + "\n");
		System.out.println("port:" + DeviceBridge.getDeviceLocalPort(device));
		
	}
}
