package com.goodluck.autotest;

import java.util.Set;

import javax.sound.midi.SysexMessage;

import com.android.ddmlib.IDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge;
import com.android.hierarchyviewerlib.device.ViewServerDevice;
import com.android.hierarchyviewerlib.device.DeviceBridge.ViewServerInfo;
import com.android.hierarchyviewerlib.models.Window;

public class Main {
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static void main(String[] args) throws InterruptedException {
        String adbLocation = isWindows() ? "adb" : "/Users/jungho/Library/Android/sdk/platform-tools/adb";
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

        IDevice device = DeviceBridge.getDevices()[0];
        if (!DeviceBridge.isViewServerRunning(device)) {
            System.out.println("startViewServer:" + DeviceBridge.startViewServer(device) + "\n");
        }

        ViewServerInfo server = DeviceBridge.loadViewServerInfo(device);
        System.out.println("protocol:" + server.protocolVersion + "\n");
        System.out.println("server:" + server.serverVersion + "\n");
        System.out.println("port:" + DeviceBridge.getDeviceLocalPort(device));

        Set<String> windows = null;
        Window appWindow = null;
        Window[] windowz = DeviceBridge.loadWindows(new ViewServerDevice(device), device);
        for (Window window : windowz) {
            windows.add(window.encode());
            System.out.println(window + ":" + window.encode() + "\n");
            if (window.toString().endsWith("Development")) {
                appWindow = window;
            }
        }

        if (!DeviceBridge.isViewServerRunning(device)) {
            System.out.println("startViewServer:" + DeviceBridge.startViewServer(device) + "\n");
        }
        
        if (appWindow != null) {
            ;
        } else {
            System.out.println("Application window not found");
        }
        
        DeviceBridge.stopViewServer(device);
        DeviceBridge.terminate();
        System.exit(0);
    }
}
