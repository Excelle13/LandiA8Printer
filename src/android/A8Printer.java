package com.ttebd.a8Printer;

import android.app.Activity;
import android.printservice.PrintService;
import android.util.Log;

import com.ttebd.a8Printer.DeviceBase;
import com.ttebd.a8Printer.PrinterMain;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.ttebd.a8Printer.DeviceBase.bindDeviceService;

/**
 * This class echoes a string called from JavaScript.
 */
public class A8Printer extends CordovaPlugin {
    private static Activity activity = null;
    PrinterMain printerMain = new PrinterMain();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        // 初始化
        if (activity == null) {
            activity = this.cordova.getActivity();
        }

        //
//    if (action.equals("coolMethod")) {
//      String message = args.getString(0);
//      this.coolMethod(message, callbackContext);
//
//      return true;
//    }

        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (action) {
                            case "coolMethod":
                                bindDeviceService(activity.getApplicationContext());
                                printerMain.init();
                                printerMain.startPrinting(activity.getApplicationContext(), callbackContext);
                        }
                    } catch (Exception e) {

                    } finally {
                        Log.e("a8printer", " execute");
                    }
                }
            };

            cordova.getThreadPool().execute(r);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
