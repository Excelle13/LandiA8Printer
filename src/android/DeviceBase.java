package com.ttebd.a8Printer;


import android.content.Context;

import com.landicorp.android.eptapi.DeviceService;
import com.landicorp.android.eptapi.exception.*;

public class DeviceBase {

    private static boolean isDeviceServiceLogined = false;

    protected boolean isDeviceServiceLogined() {
        return isDeviceServiceLogined;
    }

    public static void bindDeviceService(Context context) {
        try {
            isDeviceServiceLogined = false;
            DeviceService.login(context);
            isDeviceServiceLogined = true;
        } catch (RequestException e) {

            e.printStackTrace();
        } catch (ServiceOccupiedException e) {
            e.printStackTrace();
        } catch (ReloginException e) {
            e.printStackTrace();
        } catch (UnsupportMultiProcess e) {
            e.printStackTrace();
        }
    }

    public static void unbindDeviceService() {
        DeviceService.logout();
        isDeviceServiceLogined = false;
    }
}
