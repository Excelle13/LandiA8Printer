package com.ttebd.a8Printer;

import android.content.Context;
import android.util.Log;

import com.landicorp.android.eptapi.device.Beeper;
import com.landicorp.android.eptapi.device.Printer;
import com.landicorp.android.eptapi.device.Printer.Format;
import com.landicorp.android.eptapi.exception.RequestException;
import com.landicorp.android.eptapi.device.Printer.Alignment;

import com.landicorp.android.eptapi.utils.QrCode;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.landicorp.android.eptapi.device.Printer.Format.HZ_DOT16x16;
import static com.landicorp.android.eptapi.device.Printer.Format.HZ_DOT24x24;
import static com.landicorp.android.eptapi.utils.QrCode.ECLEVEL_Q;

public class PrinterMain extends com.ttebd.a8Printer.DeviceBase {

    private static final int FAIL = 0xff;
    private Printer.Progress progress;
    private Printer printer = Printer.getInstance();
    private List<Printer.Step> stepList;
    private Format format;

    // init stepList
    public void init() {
        stepList = new ArrayList<Printer.Step>();
    }

    // getPrinterStatus
    public int getPrinterStatus(CallbackContext callbackContext) throws RequestException {
        try {
            System.out.println(printer.getErrorDescription(Printer.getInstance().getStatus()));
            String errMessage = printer.getErrorDescription(Printer.getInstance().getStatus());
//      callbackContext.error(errMessage);
            return Printer.getInstance().getStatus();
        } catch (RequestException e) {
            // todo
//      e.printStackTrace();
        }
        // 返回自定义错误码，表示抛出异常
        return FAIL;
    }

    // 队列打印
    public void startingPrint(Context context, CallbackContext callbackContext) throws Exception {

        if (stepList == null) {
            return;
        }

        progress = new Printer.Progress() {
            @Override
            public void doPrint(Printer printer) throws Exception {

            }

            @Override
            public void onFinish(int i) {
                stepList.clear();
//        if (i == Printer.ERROR_NONE) {
//          Log.e("打印", "successful");
//        } else {
//          Log.e("打印", "failed");
//        }
                printFinish(i, callbackContext);
            }

            @Override
            public void onCrash() {
                System.out.println("onCrash");

            }
        };

//      printImg("mixc.bmp", 0, context, callbackContext);
//      printQRcode("hello", 0, 300);
        Format format = new Format();
        printiCutLine("", 5);
        printiSplitLine("", 0);

//    printTxt();

        // 将所有打印队列放置到进程中
        for (Printer.Step step : stepList) {
            progress.addStep(step);
        }

        try {
            // 开始打印
            printer.setAutoTrunc(false);
            Beeper.startBeep(50);
            progress.start();
        } catch (RequestException e) {
            Log.e("printer", "printer failed");
            unbindDeviceService();
            // todo
            e.printStackTrace();
        }


    }

    // print stepList ready
    public void generatePrintStep(JSONArray params, Context context, CallbackContext callbackContext) {

        printImg("", 0, context, callbackContext);  // print picture
        printiSplitLine("", 0);                     // print splitLine
        printiCutLine("", 0);                       // print cutLine
        printBarcode("");                                       // print barCode
        printQRcode("", 0, 300);          // print QRcode
        printTitle("", "center");                  // print single txt

//    printContent(format,)

    }

    // Print content

    /**
     * 1-可设置打印列
     * 2-可设置每一行的打印样式
     * 3-
     */
    public boolean printContent(JSONObject params, JSONObject spacing, JSONObject fontFormat) {
        if (stepList == null) {
            return false;
        }

        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {

                JSONObject hzFormat = fontFormat.getJSONObject("hzFormat");
                JSONObject ascFormat = fontFormat.getJSONObject("ascFormat");

                HzFormat(hzFormat.optString("hzScale"), hzFormat.optString("hzSize"));
                AscFormat(ascFormat.optString("ascScale"), ascFormat.optString("ascSize"));
// 两列
                if (params.length() == 2) {
                    printer.printText(String.format("" +
                                    "%" + spacing.optString("spacing1") + "s" +
                                    "%" + spacing.optString("spacing2") + "s",
                            params.optString("param1"),
                            params.optString("param2")));
                }
// 三列
                if (params.length() == 3) {
                    printer.printText(String.format("" +
                                    "%" + spacing.optString("spacing1") + "s" +
                                    "%" + spacing.optString("spacing2") + "s" +
                                    "%" + spacing.optString("spacing3") + "s",
                            params.optString("param1"),
                            params.optString("param2"),
                            params.optString("param3")));
                }

                generalFormat();
            }
        });


        return true;

    }

    // Print title
    public boolean printTitle(String text, String alignment) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {

                // 设置打印格式
                format.setAscScale(Format.AscScale.SC1x1);
                format.setAscSize(Format.AscSize.DOT24x12);
                format.setHzScale(Format.HZ_SC2x1);
                format.setHzSize(Format.HZ_DOT24x16);
                printer.setFormat(format);

                printer.setAutoTrunc(false);
                // Printing center
                if (alignment == "center") {
                    printer.printText(Printer.Alignment.CENTER, text + "\n");
                }
                // Printing left
                if (alignment == "left") {
                    printer.printText(Printer.Alignment.LEFT, text + "\n");
                }
                // Printing right
                if (alignment == "right") {
                    printer.printText(Printer.Alignment.RIGHT, text + "\n");
                }

                generalFormat();
            }
        });
        return true;
    }

    // Print barcode
    public boolean printBarcode(String barcode) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {
                printer.printBarCode(Alignment.CENTER, barcode);
                printer.printText(Alignment.CENTER, barcode + "\n");

            }
        });
        return true;
    }

    // Print QRcode
    public boolean printQRcode(String qrcode, int offset, int imgHeight) {

        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {

                QrCode qrCode = new QrCode(qrcode + "\n", ECLEVEL_Q);
                printer.printQrCode(offset, qrCode, imgHeight);
            }
        });
        return true;
    }


    // Print img
    public boolean printImg(String imgSrc, int offset, Context context, CallbackContext callbackContext) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {
                InputStream in = null;
                try {
                    in = context.getResources().getAssets().open(imgSrc);
                    printer.printImage(offset, in);
                } catch (Exception e) {
                    callbackContext.error("打印图片异常");
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    // Print cutLine
    public boolean printiCutLine(String style, int feedLine) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {
                samllFormat();
                printer.printText(Alignment.CENTER, "- - - - - - x - - - - - - - - - - - x - - - - - \n");
                printer.feedLine(feedLine);
                generalFormat();
            }
        });
        return true;
    }


    // Print splitLine
    public boolean printiSplitLine(String style, int feedLine) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {
                format.setAscScale(Format.ASC_SC1x2);
                format.setAscSize(Format.ASC_DOT16x8);
                format.setHzScale(Format.HZ_SC1x2);
                format.setHzSize(HZ_DOT16x16);
                try {
                    printer.setFormat(format);
                    printer.printText(Alignment.CENTER, "- - - - - - - - - - - - - - - - - - - - - - - - \n");
                    generalFormat();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }


    // successful printing method

    public void printFinish(int i, CallbackContext callbackContext) {
        if (i == com.landicorp.android.eptapi.device.Printer.ERROR_NONE) {
//      logUtil.info("printer", "printer success");
            Log.e("printer", "print success");
            JSONObject successMessageObj = new JSONObject();
            try {
                successMessageObj.put("code", "0000");
                successMessageObj.put("message", "打印成功");
                callbackContext.success(successMessageObj);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                unbindDeviceService();
            }
        } else {
            String errMessage = printer.getErrorDescription(i);
            Log.e("printer", "print failed：" + errMessage);
//      logUtil.info("printer", errMessage);
            JSONObject errMessageObj = new JSONObject();
            try {
                errMessageObj.put("code", "0001");
                errMessageObj.put("message", errMessage);
                callbackContext.error(errMessageObj);
            } catch (JSONException e) {
                // TODO
                e.printStackTrace();
            } finally {
                unbindDeviceService();
            }
        }
    }

    // 正常字体格式化
    public void generalFormat() {
        format.setAscScale(Format.ASC_SC1x1);
        format.setAscSize(Format.ASC_DOT24x12);
        format.setHzScale(Format.HZ_SC1x1);
        format.setHzSize(HZ_DOT24x24);
        try {
            printer.setFormat(format);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }


    // 处理字体格式-小字体1*2
    public void samllFormat() {
        format.setAscScale(Format.ASC_SC1x2);
        format.setAscSize(Format.ASC_DOT16x8);
        format.setHzScale(Format.HZ_SC1x2);
        format.setHzSize(HZ_DOT16x16);
        try {
            printer.setFormat(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // chinese format
    public void HzFormat(String hzScale, String hzSize) throws Exception {
        switch (hzScale) {
            case "SC1x1":
                format.setHzScale(Format.HzScale.SC1x1);
                break;
            case "SC1x2":
                format.setHzScale(Format.HzScale.SC1x2);
                break;
            case "SC1x3":
                format.setHzScale(Format.HzScale.SC1x3);
                break;
            case "SC2x1":
                format.setHzScale(Format.HzScale.SC2x1);
                break;
            case "SC2x2":
                format.setHzScale(Format.HzScale.SC2x2);
                break;
            case "SC2x3":
                format.setHzScale(Format.HzScale.SC2x3);
                break;
            case "SC3x1":
                format.setHzScale(Format.HzScale.SC3x1);
                break;
            case "SC3x2":
                format.setHzScale(Format.HzScale.SC3x2);
                break;
            case "SC3x3":
                format.setHzScale(Format.HzScale.SC3x3);
                break;
            default:
                format.setHzScale(Format.HzScale.SC1x1);
        }

        switch (hzSize) {
            case "DOT16x16":
                format.setHzSize(Format.HzSize.DOT16x16);
                break;
            case "DOT24x16":
                format.setHzSize(Format.HzSize.DOT24x16);
                break;
            case "DOT24x24":
                format.setHzSize(Format.HzSize.DOT24x24);
                break;
            case "DOT32x24":
                format.setHzSize(Format.HzSize.DOT32x24);
                break;
            default:
                format.setHzSize(Format.HzSize.DOT24x24);
        }

        printer.setFormat(format);


    }

    // ASC format
    public void AscFormat(String ascScale, String ascSize) throws Exception {
        switch (ascScale) {
            case "SC1x1":
                format.setAscScale(Format.AscScale.SC1x1);
                break;
            case "SC1x2":
                format.setAscScale(Format.AscScale.SC1x2);
                break;
            case "SC1x3":
                format.setAscScale(Format.AscScale.SC1x3);
                break;
            case "SC2x1":
                format.setAscScale(Format.AscScale.SC2x1);
                break;
            case "SC2x1SP":
                format.setAscScale(Format.AscScale.SC2x1SP);
                break;
            case "SC2x2":
                format.setAscScale(Format.AscScale.SC2x2);
                break;
            case "SC2x3":
                format.setAscScale(Format.AscScale.SC2x3);
                break;
            case "SC3x1":
                format.setAscScale(Format.AscScale.SC3x1);
                break;
            case "SC3x2":
                format.setAscScale(Format.AscScale.SC3x2);
                break;
            case "SC3x3":
                format.setAscScale(Format.AscScale.SC3x3);
                break;
            default:
                format.setAscScale(Format.AscScale.SC1x1);
        }


        switch (ascSize) {
            case "DOT16x8":
                format.setAscSize(Format.AscSize.DOT16x8);
                break;
            case "DOT24x12":
                format.setAscSize(Format.AscSize.DOT24x12);
                break;
            case "DOT24x8":
                format.setAscSize(Format.AscSize.DOT24x8);
                break;
            case "DOT32x12":
                format.setAscSize(Format.AscSize.DOT32x12);
                break;
            case "DOT5x7":
                format.setAscSize(Format.AscSize.DOT5x7);
                break;
            case "DOT7x7":
                format.setAscSize(Format.AscSize.DOT7x7);
                break;
            default:
                format.setAscSize(Format.AscSize.DOT24x12);
        }

        printer.setFormat(format);

    }

}
