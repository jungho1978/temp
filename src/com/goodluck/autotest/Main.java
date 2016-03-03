package com.goodluck.autotest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.android.chimpchat.ChimpChat;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.TimeoutException;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.DeviceBridge.ViewServerInfo;
import com.android.hierarchyviewerlib.device.HvDeviceFactory;
import com.android.hierarchyviewerlib.models.ViewNode;
import com.android.hierarchyviewerlib.models.Window;

public class Main {
    private static Set<ViewNode> visitedViews;
    private static String adbLocation = null;
    static {
        adbLocation = isWindows() ? "adb" : "/Users/jungho/Library/Android/sdk/platform-tools/adb";
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, AdbCommandRejectedException, IOException {
        visitedViews = new HashSet<ViewNode>();
        DeviceBridge.initDebugBridge(adbLocation);
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

        DeviceBridge.startListenForDevices(new Worker());

        IDevice device = DeviceBridge.getDevices()[0];
        if (!DeviceBridge.isViewServerRunning(device)) {
            System.out.println("startViewServer:" + DeviceBridge.startViewServer(device) + "\n");
        }

        ViewServerInfo server = DeviceBridge.loadViewServerInfo(device);
        System.out.println("protocol:" + server.protocolVersion + "\n");
        System.out.println("server:" + server.serverVersion + "\n");
        System.out.println("port:" + DeviceBridge.getDeviceLocalPort(device));

        Set<String> windows = new HashSet<String>();
        Window appWindow = null;

        Window[] windowz = DeviceBridge.loadWindows(HvDeviceFactory.create(device), device);
        for (Window window : windowz) {
            windows.add(window.encode());
            System.out.println(window + ":" + window.encode() + "\n");
            if (window.toString().endsWith("ConversationList")) {
                appWindow = window;
            }
        }

        if (!DeviceBridge.isViewServerRunning(device)) {
            System.out.println("startViewServer:" + DeviceBridge.startViewServer(device) + "\n");
        }

        if (appWindow != null) {
            ViewNode view = DeviceBridge.loadWindowData(appWindow);
            traverseView(device, appWindow, view);
        } else {
            System.out.println("Application window not found");
        }

        DeviceBridge.stopViewServer(device);
        DeviceBridge.terminate();
        System.exit(0);
    }

    private static void traverseView(IDevice device, Window window, ViewNode view) {
        if (visitedViews.contains(view)) {
            return;
        }

        visitedViews.add(view);

        if (view.name.contains("TextView") || view.name.contains("Button")) {
            Map<String, String> options = new TreeMap<String, String>();
            options.put("backend", "adb");
            options.put("adbLocation", adbLocation);
            ChimpChat chimpChat = ChimpChat.getInstance(options);
        }
        for (ViewNode child : view.children) {
            traverseView(device, window, child);
        }
    }

    private static Position getViewTopLeft(ViewNode view) {
        if (view.parent != null) {
            Position pos = getViewTopLeft(view.parent);
            pos.x += view.left;
            pos.y += view.top;
            return pos;
        }
        return new Position(view.left, view.top);
    }
}
