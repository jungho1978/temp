package com.goodluck.autotest;

import com.android.chimpchat.hierarchyviewer.HierarchyViewer;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.device.ViewServerDevice;
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
                ViewNode view = DeviceBridge.loadWindowData(w);
                traverseView(device, w, view);
            }
        }

        DeviceBridge.stopViewServer(device);
        AndroidDebugBridge.terminate();

        System.exit(0);
    }

    private static void traverseView(IDevice device, Window window, ViewNode view) {
        System.out.println(HierarchyViewer.getAbsolutePositionOfView(view));
        for (ViewNode c : view.children) {
            traverseView(device, window, c);
        }
    }
}
