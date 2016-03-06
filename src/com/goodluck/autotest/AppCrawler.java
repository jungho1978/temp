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

    private static Set<String> sVisitedViews = new HashSet<String>();

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
                System.out.println("Target App [" + getPkgName(w) + "]");
                String pkgName = getPkgName(w);
                //ViewNode view = DeviceBridge.loadWindowData(w);
                //printViewInfo(view);
                IChimpDevice chimpDevice = new AdbChimpDevice(device);
                ViewNode view = DeviceBridge.loadWindowData(w);
                traverseView(chimpDevice, device, pkgName, w, view);
            }
        }

        DeviceBridge.stopViewServer(device);
        AndroidDebugBridge.terminate();

        System.exit(0);
    }

    private static void traverseView(IChimpDevice chimpDevice, IDevice device, String pkgName, Window window, ViewNode view) throws InterruptedException {
        if (!getPkgName(window).contains(pkgName)) {
            return;
        }

        if (sVisitedViews.contains(view.toString())) {
            System.out.println(view.toString());
            return;
        }
        sVisitedViews.add(view.toString());

        boolean isVisible = view.namedProperties.get("getVisibility()").value.equals("VISIBLE");
        boolean isClickable = view.namedProperties.get("isClickable()").value.equals("true");

        if (isVisible && isClickable) {
            System.out.println("---------------------------------");
            System.out.println(toViewString(view));
            System.out.println("---------------------------------");
            Point point = HierarchyViewer.getAbsoluteCenterOfView(view);
            chimpDevice.touch(point.x, point.y, TouchPressType.DOWN_AND_UP);
            Thread.sleep(3000);

            Window[] windows = DeviceBridge.loadWindows(HvDeviceFactory.create(device), device);
            for (Window w : windows) {
                ViewNode v = DeviceBridge.loadWindowData(w);
                traverseView(chimpDevice, device, pkgName, w, v);
            }

            chimpDevice.press(PhysicalButton.BACK, TouchPressType.DOWN_AND_UP);
        }

        for (ViewNode c : view.children) {
            traverseView(chimpDevice, device, pkgName, window, c);
        }
    }

    private static String getPkgName(Object obj) {
        String pkgName = null;
        try {
            if (obj instanceof Window) {
                return ((Window)obj).getTitle().split("/")[0];
            } else if (obj instanceof ViewNode) {
                return ((ViewNode)obj).window.getTitle().split("/")[0];
            }
        } catch (Exception e) {
            return null;
        }
        return pkgName;
    }
    
    private static String toViewString(ViewNode view) {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("[name] " + view.name + "\n");
        sBuilder.append("[hasCode] " + view.hashCode + "\n");
        sBuilder.append("[viewId] " + view.id + "\n");
        System.out.println("[Visible] " + view.namedProperties.get("getVisibility()").value + "");
        System.out.println("[Clickable] " + view.namedProperties.get("isClickable()").value);
        return sBuilder.toString();
    }
    
    private static void printViewInfo(ViewNode view) {
        System.out.println("-----------------------");
        System.out.println("[name] " + view.name);
        System.out.println("[hashCode] " + view.hashCode);
        System.out.println("[viewId] " + view.id);
        System.out.println("[Visible] " + view.namedProperties.get("getVisibility()").value);
        System.out.println("[Clickable] " + view.namedProperties.get("isClickable()").value);
        System.out.println("-----------------------");
        for (ViewNode v : view.children) {
            printViewInfo(v);
        }
    }
}
