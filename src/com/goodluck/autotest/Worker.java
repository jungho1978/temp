package com.goodluck.autotest;

import java.util.ArrayList;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDebugBridgeChangeListener;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

public class Worker implements IDebugBridgeChangeListener, IDeviceChangeListener {
	private ArrayList<IDevice> devices;
	
	public Worker() {
		devices = new ArrayList<IDevice>();
	}

	@Override
	public void bridgeChanged(AndroidDebugBridge bridge) {
		System.out.println("bridge connected\n");
	}

	@Override
	public void deviceChanged(IDevice device, int changeMask) {
		System.out.print(device + " status changed to ");
		System.out.print(changeMask);
		System.out.print("\n");
	}

	@Override
	public void deviceConnected(IDevice device) {
		System.out.println(device + " connected\n");
		devices.add(device);
	}

	@Override
	public void deviceDisconnected(IDevice device) {
		System.out.print(device);
		System.out.println(" disconnected\n");
	}
}
