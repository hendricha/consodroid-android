package hu.hendricha.consodroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DeviceInfoWriter {
    public static void writeDeviceInfo(File file, Activity activity) {
        JSONObject info = new JSONObject();

        try {
            info.put("batteryPercentage", getBatteryPercentage(activity));
            info.put("operator", getOperator(activity));
            info.put("diskInfo", getDiskInfo());
            info.put("product", getProduct());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(info.toString());
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("ConsoDroid", "deviceInfoWriter: wrote out device info ");
    }

    public static float getBatteryPercentage(Activity activity) {
        Intent batteryIntent = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return ((float)level / (float)scale) * 100.0f;
    }

    private static String getOperator(Activity activity) {
        TelephonyManager telephonyManager = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE));
        return telephonyManager.getNetworkOperatorName();
    }

    private static JSONObject getDiskInfo() throws JSONException {
        JSONObject info = new JSONObject();

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long megBytesCount = (long)stat.getBlockSize() *(long)stat.getBlockCount() / 1048576;
        long megBytesAvailable = (long)stat.getBlockSize() *(long)stat.getAvailableBlocks() / 1048576;

        info.put("size", megBytesCount);
        info.put("availableSpace", megBytesAvailable);

        return info;
    }

    public static String getProduct() {
        return Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.PRODUCT + ")";
    }
}
