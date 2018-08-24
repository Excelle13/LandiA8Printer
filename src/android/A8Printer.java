package com.ttebd.a8Printer;

import android.app.Activity;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.ttebd.a8Printer.DeviceBase.bindDeviceService;
import static com.ttebd.a8Printer.DeviceBase.unbindDeviceService;

/**
 * This class echoes a string called from JavaScript.
 */
public class A8Printer extends CordovaPlugin {
    private static Activity activity = null;
    com.ttebd.a8Printer.PrinterMain printerMain = new com.ttebd.a8Printer.PrinterMain();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        // 初始化
        if (activity == null) {
            activity = this.cordova.getActivity();
        }

        System.out.println("params--->>" + args);


        // 打印状态
//    int printerStatusCode = 0;
//    try {
//      printerStatusCode = printer.getStatus();
//    } catch (RequestException e) {
//      //TODO
//      callbackContext.error("打印机错误待定");
//    }
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (action) {

                            //Test method
                            case "coolMethod":
                                JSONObject testParams = args.getJSONObject(0);
                                coolMethod(testParams.toString(), callbackContext);
                                break;

                            // main print method
                            case "printMain":
                                bindDeviceService(activity.getApplicationContext());
                                printerMain.init();
                                printerMain.startingPrint(args, activity.getApplicationContext(), callbackContext);
                                break;

                            /** Print resource release **/
                            case "printResRelease":
                                unbindDeviceService();
                                break;
                        }
                    } catch (Exception e) {
                        unbindDeviceService();

                    } finally {
                        Log.e("a8printer", " execute");
                    }
                }
            };
            cordova.getThreadPool().execute(r);
            return true;
        } catch (Exception e) {
            unbindDeviceService();
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
