package com.github.simplezbar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SimpleZBar extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            if (action.equals("scanFromFile")) {
                String fileName = args.getString(0);
                this.scanFromFile(fileName, callbackContext);
                return true;
            } else if (action.equals("scanFromDataUrl")) {
                String dataUrl = args.getString(0);
                this.scanFromDataUrl(dataUrl, callbackContext);
                return true;
            }
        } catch (JSONException e) {
            callbackContext.error("Error manipulating JSON data.");
        } catch (IOException e) {
            callbackContext.error("Error during IO.");
        }

        return false;
    }

    private JSONArray scanFromBitmap(Bitmap bmp) throws JSONException, IOException {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] intArray = new int[width * height];

        bmp.getPixels(intArray, 0, width, 0, 0, width, height);

        ImageScanner scanner = new ImageScanner();
        Image barcode = new Image(width, height, "RGB4");

        barcode.setData(intArray);
        scanner.scanImage(barcode.convert("Y800"));
        SymbolSet ss = scanner.getResults();
        JSONArray qrs = new JSONArray();

        for (Symbol s : ss) {
            JSONObject obj = new JSONObject();
            obj.put("data", s.getData());
            obj.put("x", s.getBounds()[0]);
            obj.put("y", s.getBounds()[1]);
            obj.put("width", s.getBounds()[2]);
            obj.put("height", s.getBounds()[3]);

            qrs.put(obj);
        }

        return qrs;
    }

    private void scanFromFile(String fileName, CallbackContext callbackContext) throws JSONException, IOException {
        if (fileName != null && fileName.length() > 0) {
            Bitmap bmp = BitmapFactory.decodeFile(fileName);
            if (bmp == null)
                throw new IOException();

            JSONArray obj = scanFromBitmap(bmp);
            callbackContext.success(obj.toString());
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void scanFromDataUrl(String dataUrl, CallbackContext callbackContext) throws JSONException, IOException {
        if (dataUrl != null && dataUrl.length() > 0) {
            String encodingPrefix = "base64,";
            int contentStartIndex = dataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
            byte[] data = Base64.decode(dataUrl.substring(contentStartIndex), Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bmp == null)
                throw new IOException();

            JSONArray obj = scanFromBitmap(bmp);
            callbackContext.success(obj.toString());
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
