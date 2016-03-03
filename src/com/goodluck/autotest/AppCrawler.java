package com.goodluck.autotest;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.Point;

import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.android.chimpchat.hierarchyviewer.HierarchyViewer;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

public class AppCrawler {
    private static String sAdbLocation = null;
    static {
        sAdbLocation = isWindow() ? "adb" : "/Users/jungho/Library/Android/sdk/platform-tools/adb";
    }

    private static boolean isWindow() {
        return System.getProperty("os.name").startsWith("Windows");
    }
    
    private static Set<ViewNode> sVisitedViews = new HashSet<ViewNode>();
    
    public static void main(String[] args) throws InterruptedException {
        AndroidDebugBridge.init(false);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(sAdbLocation, true);
        int retryCnt = 5;
        do {
            Thread.sleep(500);
            System.out.println("Waiting for device...");
        } while (--retryCnt > 0 && bridge.getDevices().length == 0);

        if (bridge.getDevices().length == 0) {
            System.out.println("No device found");
            return;
        }

        IDevice device = bridge.getDevices()[0];
        DeviceBridge.setupDeviceForward(device);

        if (!DeviceBridge.isViewServerRunning(device)) {
            System.out.println("Start view server: " + DeviceBridge.startViewServer(device));
        }

        DeviceBridge.loadViewServerInfo(device);
        int id = DeviceBridge.getFocusedWindow(device);
        Window[] windows = DeviceBridge.loadWindows(HvDeviceFactory.create(device), device);
        for (Window w : windows) {
            if (w.getHashCode() == id) {
                System.out.println(w.getTitle());
                IChimpDevice chimpDevice = new AdbChimpDevice(device);
                ViewNode view = DeviceBridge.loadWindowData(w);
                traverseView(chimpDevice, device, w, view);
            }
        }

        DeviceBridge.stopViewServer(device);
        AndroidDebugBridge.terminate();

        System.exit(0);
    }

    private static void traverseView(IChimpDevice chimpDevice, IDevice device, Window window, ViewNode view) throws InterruptedException {
    	if (sVisitedViews.contains(view)) {
    		System.out.println(view.name + " has been already visited");
    		return;
    	}
    	sVisitedViews.add(view);
    	
    	if (view.name.contains("TextView") || view.name.contains("Button")) {
    		Point point = HierarchyViewer.getAbsoluteCenterOfView(view);
    		chimpDevice.touch(point.x, point.y, TouchPressType.DOWN_AND_UP);
    		Thread.sleep(3000);
    		
    		Window[] windows = DeviceBridge.loadWindows(HvDeviceFactory.create(device), device);
    		for (Window w : windows) {
    			traverseView(chimpDevice, device, w, view);
    		}
    		
    		chimpDevice.press(PhysicalButton.BACK, TouchPressType.DOWN_AND_UP);
    	}
        for (ViewNode c : view.children) {
            traverseView(chimpDevice, device, window, c);
        }
    }
}
