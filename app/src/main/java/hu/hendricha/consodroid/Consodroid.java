package hu.hendricha.consodroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Consodroid extends Activity {

    private Process nodeProcess;
    private FileObserver accessControlObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consodroid);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("ConsoDroid", "onResume");
        if (this.requiresAssetIstall()) {
            Log.d("ConsoDroid", "Assets need to be installed");
            launchRingDialog();
        } else {
            Log.d("ConsoDroid", "Assets do not need to be installed");
        }

        TextView ipValue = (TextView)findViewById(R.id.ip_value);
        ipValue.setText(IpUtil.getIdealIPAddress());

        createAccessControlObserver();
    }

    private boolean requiresAssetIstall() {
        File file = new File(this.getFilesDir(), "node/version");
        if (!file.exists()) {
            return true;
        }

        AssetManager manager = getAssets();
        try {
            InputStream versionFile = manager.open("node/version");
            InputStream installedVersionFile = new FileInputStream(file);
            String version = readFile(versionFile);
            String installedVersion = readFile(installedVersionFile);
            if (!version.equals(installedVersion)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String readFile(InputStream fileStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(fileStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    public void launchRingDialog() {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(this, "Please wait ...", "Installing assets ...", true);
        ringProgressDialog.setCancelable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyAsset(getFilesDir(), "node");
                Process nativeApp = null;
                try {
                    nativeApp = Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/hu.hendricha.consodroid/files/node/node");
                    nativeApp.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ringProgressDialog.dismiss();
            }
        }).start();
    }

    private void createAccessControlObserver() {
        final File dir = new File(this.getFilesDir(), "accessControl");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        accessControlObserver = new FileObserver("/data/data/hu.hendricha.consodroid/files/accessControl") {
            @Override
            public void onEvent(int event, final String fileName) {
                Log.d("ConsoDroid", "FileObserver event: " + fileName);

                if(event == FileObserver.CREATE){ // check if its a "create" and not equal to .probe because thats created every time camera is launched
                    Log.d("ConsoDroid", "Found new access control file: " + fileName);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(Consodroid.this)
                                .setTitle("New connection")
                                .setMessage("Do you allow " + fileName + " access to all folders?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(dir, fileName)));
                                            outputStreamWriter.write("true");
                                            outputStreamWriter.close();
                                            Log.d("ConsoDroid", "Gave access to: " + fileName);
                                        }
                                        catch (IOException e) {
                                            Log.e("Exception", "File write failed: " + e.toString());
                                        }
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                        }
                    });
                }
            }
        };
        accessControlObserver.startWatching();
    }

    @Override
    protected void onPause() {
        accessControlObserver.stopWatching();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.consodroid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            openAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openAboutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle("About ConsoDroid");
        TextView textView = (TextView)dialog.findViewById(R.id.license_line_3);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        dialog.show();
    }

    /**
     * Copy the asset at the specified path to this app's data directory. If the
     * asset is a directory, its contents are also copied.
     *
     * @param path
     * Path to asset, relative to app's assets directory.
     */
    private void copyAsset(File parentDir, String path) {
        Log.d("ConsoDroid", "Copying asset: " + parentDir.getAbsolutePath() + "  /  " + path);
        AssetManager manager = getAssets();

        // If we have a directory, we make it and recurse. If a file, we copy its
        // contents.
        try {
            String asset = parentDir.getAbsolutePath().replace(getFilesDir().getAbsolutePath(), "") + "/" + path;
            String[] contents = manager.list(asset.charAt(0) == '/' ? asset.substring(1) : asset);

            // The documentation suggests that list throws an IOException, but doesn't
            // say under what conditions. It'd be nice if it did so when the path was
            // to a file. That doesn't appear to be the case. If the returned array is
            // null or has 0 length, we assume the path is to a file. This means empty
            // directories will get turned into files.
            if (contents == null || contents.length == 0)
                throw new IOException();

            // Make the directory.
            File dir = new File(parentDir, path);
            dir.mkdirs();

            // Recurse on the contents.
            for (String entry : contents) {
                copyAsset(dir, entry);
            }
        } catch (IOException e) {
            copyFileAsset(parentDir, path);
        }
    }

    /**
     * Copy the asset file specified by path to app's data directory. Assumes
     * parent directories have already been created.
     *
     * @param path
     * Path to asset, relative to app's assets directory.
     */
    private void copyFileAsset(File parentDir, String path) {
        Log.d("ConsoDroid", "Copying file: " + parentDir.getAbsolutePath() + "  /  " + path);
        File file = new File(parentDir, path);

        try {
            String asset = parentDir.getAbsolutePath().replace(getFilesDir().getAbsolutePath(), "") + "/" + path;
            InputStream in = getAssets().open(asset.charAt(0) == '/' ? asset.substring(1) : asset);
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConsoDroid", e.getMessage());
        }
    }

    public void onSwitchClicked(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();

        if (on) {
            try {
                nodeProcess = Runtime.getRuntime().exec("/data/data/hu.hendricha.consodroid/files/node/node subdir/hello.js", new String[0], new File("/data/data/hu.hendricha.consodroid/files/node"));
                Log.d("Consodroid", "Started node");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (nodeProcess != null) {
                nodeProcess.destroy();
                Log.d("ConsoDroid", "Stopped node");
            }
        }
    }
}
