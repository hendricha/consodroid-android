package hu.hendricha.consodroid;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class PackageInfoWriter {
    public static void writeInstalledApps(File file, PackageManager packageManager) {
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);

            //Don't get system packages. It is a bit hacky, needs better workaround.
            if (p.versionName == null ||
                packageManager.getLaunchIntentForPackage(p.packageName) == null ||
                p.packageName.startsWith("com.android")) {
                continue;
            }

            JSONObject newInfo = new JSONObject();
            try {
                newInfo.put("label", p.applicationInfo.loadLabel(packageManager).toString());
                newInfo.put("name", p.packageName);
                //newInfo.put("icon", p.applicationInfo.loadIcon(packageManager));
                jsonArray.put(newInfo);

                Log.d("ConsoDroid", "packageInfoWriter: found package " + p.packageName);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }

        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(jsonArray.toString());
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Log.d("ConsoDroid", "packageInfoWriter: wrote out packages ");
    }
}
