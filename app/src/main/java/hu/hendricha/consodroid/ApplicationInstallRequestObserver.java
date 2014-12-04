package hu.hendricha.consodroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ApplicationInstallRequestObserver extends AbstractObserver {
    protected String folderName = "apkInstallRequests";

    public ApplicationInstallRequestObserver(Activity activity) {
        super(activity);
    }

    @Override
    protected void manageCreateEvent(File dir, String fileName) {
        Log.d("ConsoDroid", "Found new application install/uninstall request file: " + fileName);
        String apkFilePath = readFromFile(new File(dir, fileName));
        Log.d("ConsoDroid", "Requesting install of: " + apkFilePath);
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.parse("file://" + apkFilePath), "application/vnd.android.package-archive");
        activity.startActivity(promptInstall);
    }

    private String readFromFile(File file) {
        String result = "";

        try {
            InputStream inputStream = new FileInputStream(file);

            if ( inputStream != null ) {
                Log.e("Exception", "IF");
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
