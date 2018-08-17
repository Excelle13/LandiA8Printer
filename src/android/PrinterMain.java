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
import java.util.Iterator;
import java.util.List;

import static com.landicorp.android.eptapi.utils.QrCode.ECLEVEL_Q;

public class PrinterMain extends com.ttebd.a8Printer.DeviceBase {

    private static final int FAIL = 0xff;
    private Printer.Progress progress;
    private Printer printer = Printer.getInstance();
    private List<Printer.Step> stepList;

    // init stepList
    public void init() {
        stepList = new ArrayList<Printer.Step>();
    }

    // getPrinterStatus
    public int getPrinterStatus() {
        try {
//      System.out.println(printer.getErrorDescription(Printer.getInstance().getStatus()));
//      String errMessage = printer.getErrorDescription(Printer.getInstance().getStatus());
//      callbackContext.error(errMessage);

            return Printer.getInstance().getStatus();
        } catch (RequestException e) {
//      callbackContext.error("getPrinterStatus--" + e.getMessage());

            // todo
//      e.printStackTrace();
        }
        // 返回自定义错误码，表示抛出异常
        return FAIL;
    }

    // 队列打印
    public void startingPrint(JSONArray params, Context context, CallbackContext callbackContext) throws Exception {
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

        Format format = new Format();


//      printImg("mixc.bmp", 0, context, callbackContext);
//      printQRcode("hello", 0, 300);
//    Format format = new Format();
//    printiCutLine("", 2, format);
//    printiSplitLine("", 0, format);

//    printTxt();


        //产生所以需要打印的步骤
        generatePrintStep(params, format, context, callbackContext);

        // 将所有打印队列放置到进程中
        for (Printer.Step step : stepList) {
            progress.addStep(step);
        }


        if (getPrinterStatus() == 0) {
            try {
                // 开始打印
                Beeper.startBeep(50);
                progress.start();
            } catch (RequestException e) {
                Log.e("printer", "printer failed");
                unbindDeviceService();
                // todo
                e.printStackTrace();
            }
        } else {
            System.err.println("打印狀態不能與0----------");
            printFinish(getPrinterStatus(), callbackContext);
        }


    }

    // print stepList ready
    public void generatePrintStep(JSONArray params, Format format, Context context, CallbackContext callbackContext) throws JSONException {

        JSONObject obj = params.getJSONObject(0);

        Iterator iterator = obj.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
//      String value = obj.getString(key);

            // If there are pictures in the parameter, increase the steps of printing pictures.
            if (key.indexOf("img") == 0) {
                String imgSrc = "";
                int imgOffset = 0;

                JSONObject imgObj = obj.getJSONObject(key);

                Iterator imgObjIterator = imgObj.keys();

                while (imgObjIterator.hasNext()) {
                    String titleKey = (String) imgObjIterator.next();
                    if (titleKey.indexOf("imgSrc") == 0) {
                        imgSrc = imgObj.optString(titleKey);
                    }
                    if (titleKey.indexOf("imgOffset") == 0) {
                        imgOffset = imgObj.optInt(titleKey);
                    }
                }

                printImg(imgSrc, imgOffset, context, callbackContext);  // print picture
            }

            // If there are headings in the parameter, add print heading steps.
            if (key.indexOf("title") == 0) {

                String titleTxt = "";
                String titleAlign = "";
                JSONObject spaceXY = null;
                JSONObject titleFontFormat = null;

                JSONObject titleObj = obj.getJSONObject(key);

                Iterator titleObjIterator = titleObj.keys();

                while (titleObjIterator.hasNext()) {
                    String titleKey = (String) titleObjIterator.next();
                    if (titleKey.indexOf("titleTxt") == 0) {
                        titleTxt = titleObj.optString(titleKey);
                    }
                    if (titleKey.indexOf("titleAlign") == 0) {
                        titleAlign = titleObj.optString(titleKey);
                    }
                    if (titleKey.indexOf("spaceXY") == 0) {
                        spaceXY = titleObj.getJSONObject(titleKey);
                    }

                    if (titleKey.indexOf("fontFormat") == 0) {
                        titleFontFormat = titleObj.getJSONObject(titleKey);
                    }
                }
                printTitle(titleTxt, titleAlign, titleFontFormat, spaceXY, format);                  // print title

            }

            // If there are split lines in the parameter, increase the steps of print segmentation.
            if (key.indexOf("splitLine") == 0) {
                printiSplitLine("", 0, format);                     // print splitLine
            }

            // If there are cut lines in the parameter, increase the steps of print segmentation.
            if (key.indexOf("cutLine") == 0) {
                printiCutLine("", 0, format);                       // print cutLine
            }

            //content
            if (key.indexOf("content") == 0) {
                JSONObject contentParams = null;
                JSONObject contentSpacing = null;
                JSONObject contentFontFormat = null;
                JSONObject spaceXY = null;


                JSONArray contentArray = obj.getJSONArray(key);


                for (int i = 0; i < contentArray.length(); i++) {
                    JSONObject contents = contentArray.getJSONObject(i);
                    Iterator contentIterator = contents.keys();

                    while (contentIterator.hasNext()) {
                        String contentKey = (String) contentIterator.next();

                        if (contentKey.indexOf("params") == 0) {
                            contentParams = contents.getJSONObject(contentKey);
                        }

                        if (contentKey.indexOf("spacing") == 0) {
                            contentSpacing = contents.getJSONObject(contentKey);
                        }

                        if (contentKey.indexOf("fontFormat") == 0) {
                            contentFontFormat = contents.getJSONObject(contentKey);
                        }
                        if (contentKey.indexOf("spaceXY") == 0) {
                            spaceXY = contents.getJSONObject(contentKey);

                        }

                    }

                }


//        System.out.println(contentArray);

//        JSONArray contentArray1= contentArray.getJSONArray(obj);
                printContent(contentParams, contentSpacing, spaceXY, contentFontFormat, format);
            }

            //barCode
            if (key.indexOf("barCode") == 0) {
                String barCodeObj = obj.optString(key);
                printBarcode(barCodeObj);                                       // print barCode
            }

            // qrcode
            if (key.indexOf("qrCode") == 0) {
                JSONObject qrCodeObj = obj.getJSONObject(key);
                System.out.println("qrcode" + qrCodeObj);
                String qrcode = "";
                int qrCodeOffset = 0;
                int qrCodeImgHeight = 300;

                Iterator qrCodeiterator = obj.keys();

                while (qrCodeiterator.hasNext()) {

                    String qrCodeKey = (String) qrCodeiterator.next();

                    if (qrCodeKey.indexOf("qrcode") == 0) {
                        qrcode = qrCodeObj.optString(qrCodeKey);
                    }
                    if (qrCodeKey.indexOf("qrCodeOffset") == 0) {
                        qrCodeOffset = qrCodeObj.optInt(qrCodeKey);
                    }
                    if (qrCodeKey.indexOf("qrCodeImgHeight") == 0) {
                        qrCodeImgHeight = qrCodeObj.optInt(qrCodeKey);
                    }
                }
                printQRcode(qrcode, qrCodeOffset, qrCodeImgHeight);          // print QRcode
            }

            //test
//      System.out.println("jsonkey-----" + value);
        }


//    printContent(format,)

    }

    // Print content

    /**
     * 1-可设置打印列
     * 2-可设置每一行的打印样式
     * 3-
     */
    public boolean printContent(JSONObject params, JSONObject spacing, JSONObject spaceXY, JSONObject fontFormat, Format format) {
        if (stepList == null) {
            return false;
        }

        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {
                printer.setAutoTrunc(false);

                // 设置字行间距
                format.setXSpace(spaceXY.optInt("spaceX"));
                format.setYSpace(spaceXY.optInt("spaceY"));

                JSONObject hzFormat = fontFormat.getJSONObject("hzFormat");
                JSONObject ascFormat = fontFormat.getJSONObject("ascFormat");

                HzFormat(hzFormat.optString("hzScale"), hzFormat.optString("hzSize"), format, printer);
                AscFormat(ascFormat.optString("ascScale"), ascFormat.optString("ascSize"), format, printer);
// 两列
                if (params.length() == 2) {
                    printer.printText(String.format("" +
                                    "%" + spacing.optString("spacing1") + "s" +
                                    "%" + spacing.optString("spacing2") + "s",
                            params.optString("param1"),
                            params.optString("param2")) + "\n");
                }
// 三列
                if (params.length() == 3) {
                    printer.printText(String.format("" +
                                    "%" + spacing.optString("spacing1") + "s" +
                                    "%" + spacing.optString("spacing2") + "s" +
                                    "%" + spacing.optString("spacing3") + "s",
                            params.optString("param1"),
                            params.optString("param2"),
                            params.optString("param3")) + "\n");
                }

                generalFormat(format, printer);
            }
        });


        return true;

    }

    // Print title
    public boolean printTitle(String text, String alignment, JSONObject fontFormat, JSONObject spaceXY, Format format) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {

                printer.setAutoTrunc(false);
//        format.setXSpace();
//        format.setYSpace();

                // 设置打印格式
//        format.setAscScale(Format.AscScale.SC1x1);
//        format.setAscSize(Format.AscSize.DOT24x12);
//        format.setHzScale(Format.HZ_SC2x1);
//        format.setHzSize(Format.HZ_DOT24x16);
//        printer.setFormat(format);

                // 设置字行间距
                format.setXSpace(spaceXY.optInt("spaceX"));
                format.setYSpace(spaceXY.optInt("spaceY"));
                JSONObject hzFormat = fontFormat.getJSONObject("hzFormat");
                JSONObject ascFormat = fontFormat.getJSONObject("ascFormat");

                HzFormat(hzFormat.optString("hzScale"), hzFormat.optString("hzSize"), format, printer);
                AscFormat(ascFormat.optString("ascScale"), ascFormat.optString("ascSize"), format, printer);

                // Printing center
                if (alignment.equals("center")) {
                    printer.printText(Printer.Alignment.CENTER, text + "\n");
                }
                // Printing left
                if (alignment.equals("left")) {
                    printer.printText(Printer.Alignment.LEFT, text + "\n");
                }
                // Printing right
                if (alignment.equals("right")) {
                    printer.printText(Printer.Alignment.RIGHT, text + "\n");
                }

                generalFormat(format, printer);
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
                printer.setAutoTrunc(false);

                QrCode qrCode = new QrCode(qrcode, ECLEVEL_Q);
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
    public boolean printiCutLine(String style, int feedLine, Format format) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {
                samllFormat(format, printer);
                printer.printText(Alignment.CENTER, "- - - - - - x - - - - - - - - - - - x - - - - - \n");
                printer.feedLine(feedLine);
                generalFormat(format, printer);
            }
        });
        return true;
    }


    // Print splitLine
    public boolean printiSplitLine(String style, int feedLine, Format format) {
        if (stepList == null) {
            return false;
        }
        stepList.add(new Printer.Step() {
            @Override
            public void doPrint(Printer printer) throws Exception {
                samllFormat(format, printer);
                try {
                    printer.setFormat(format);
                    printer.printText(Alignment.CENTER, "- - - - - - - - - - - - - - - - - - - - - - - - \n");
                    printer.feedLine(feedLine);
                    generalFormat(format, printer);
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
    public void generalFormat(Format format, Printer printer) {
        format.setYSpace(0);
        format.setXSpace(0);
        format.setAscScale(Format.ASC_SC1x1);
        format.setAscSize(Format.ASC_DOT24x12);
        format.setHzScale(Format.HZ_SC1x1);
        format.setHzSize(Format.HZ_DOT24x24);
        try {
            printer.setFormat(format);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }


    // 处理字体格式-小字体1*2
    public void samllFormat(Format format, Printer printer) {
        format.setAscScale(Format.ASC_SC1x2);
        format.setAscSize(Format.ASC_DOT16x8);
        format.setHzScale(Format.HZ_SC1x2);
        format.setHzSize(Format.HZ_DOT16x16);
        try {
            printer.setFormat(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // chinese format
    public void HzFormat(String hzScale, String hzSize, Format format, Printer printer) throws Exception {
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
    public void AscFormat(String ascScale, String ascSize, Format format, Printer printer) throws Exception {
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
