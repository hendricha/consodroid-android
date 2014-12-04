package hu.hendricha.consodroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.FileObserver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ApplicationInstallRequestObserver extends AbstractObserver {
    public ApplicationInstallRequestObserver(Activity activity) {
        super(activity, "apkInstallRequests", FileObserver.CLOSE_WRITE);
    }

    @Override
    protected void manageEvent(File dir, String fileName) {
        Log.d("ConsoDroid", "Found new application install/uninstall request file: " + fileName);
        String fileContents = readFromFile(new File(dir, fileName));
        String[] parts = fileName.split("-");

        Intent newActivity;
        if (parts[0].equals("install")) {
            String apkFilePath = fileContents;
            Log.d("ConsoDroid", "Requesting install of: " + apkFilePath);
            newActivity = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.parse("file://" + apkFilePath), "application/vnd.android.package-archive");
        } else if (parts[0].equals("uninstall")) {
            String packageName = fileContents;
            Log.d("ConsoDroid", "Requesting uninstall of: " + packageName);
            newActivity = new Intent(Intent.ACTION_DELETE)
                    .setData(Uri.parse("package:" + packageName));
        } else {
            Log.d("ConsoDroid", "Unknown application management request: " + fileContents);
            return;
        }

        activity.startActivity(newActivity);
    }

    private String readFromFile(File file) {
        String result = "";

        try {
            InputStream inputStream = new FileInputStream(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                   stringBuilder.append(receiveString);
                }

                inputStream.close();
                result = stringBuilder.toString();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "Can not read file: " + e.toString());
        }

        return result;
    }
}
