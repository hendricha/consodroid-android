package hu.hendricha.consodroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class AccessControlObserver extends AbstractObserver {
    public AccessControlObserver(Activity activity) {
        super(activity, "accessControl");
    }

    protected void manageCreateEvent(final File dir, final String fileName) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(activity)
                        .setTitle(activity.getString(R.string.new_connection))
                        .setMessage(String.format(activity.getString(R.string.allow_x), fileName))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(dir, fileName)));
                                    outputStreamWriter.write("true");
                                    outputStreamWriter.close();
                                    Log.d("ConsoDroid", "Gave access to: " + fileName);
                                } catch (IOException e) {
                                    Log.e("Exception", "File write failed: " + e.toString());
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
    }
}
