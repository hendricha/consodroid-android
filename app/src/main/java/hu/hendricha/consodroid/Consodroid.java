package hu.hendricha.consodroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Looper;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
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
    private ProgressDialog ringProgressDialog = null;
    private int assetNumber = 0;
    private String mountedObbPath = "";
    private OnObbStateChangeListener obbStateChangeListener = new OnObbStateChangeListener() {
        @Override
        public void onObbStateChange(final String path, int state) {
            super.onObbStateChange(path, state);
            Log.d("ConsoDroid", "OBB state change: path: " + path + "; state: " + state);
        }
    };
    private String obbPathOnExternalStorage = "/Android/obb/hu.hendricha.consodroid";

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
            launchCopyAssetDialog();
        } else {
            Log.d("ConsoDroid", "Assets do not need to be installed");
            mountObb();
        }

        updateIpAddress();

        createAccessControlObserver();
    }

    private boolean requiresAssetIstall() {
        File file = new File(Environment.getExternalStorageDirectory(), obbPathOnExternalStorage + "/version");
        if (!file.exists()) {
            return true;
        }

        AssetManager manager = getAssets();
        try {
            InputStream versionFile = manager.open("version");
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

    public void launchCopyAssetDialog() {
        if (ringProgressDialog != null) {
            Log.e("ConsoDroid", "There was already a copy asset dialog open.");
            return;
        }
        ringProgressDialog = ProgressDialog.show(this, "Please wait, installing assets...", "Installing assets ...", true);
        ringProgressDialog.setCancelable(false);
        ringProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                unmountObb();
                File obbDirectory = new File(Environment.getExternalStorageDirectory(), obbPathOnExternalStorage);
                if (!obbDirectory.exists()) {
                    obbDirectory.mkdirs();
                }
                copyFileAsset(obbDirectory, "version");
                copyFileAsset(obbDirectory, "consodroid.obb");
                mountObb();

                ringProgressDialog.dismiss();
                ringProgressDialog = null;
            }
        }).start();
    }

    private void mountObb() {
        final StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        String obbPath = Environment.getExternalStorageDirectory() + "/Android/obb";
        final String obbFilePath = obbPath + "/hu.hendricha.consodroid/consodroid.obb";

        if (storageManager.isObbMounted(obbFilePath)) {
            setObbPath(storageManager.getMountedObbPath(obbFilePath));
            Log.d("ConsoDroid", "obb is already mounted to " + mountedObbPath);
            return;
        }

        Log.d("ConsoDroid", "obb: will now atempt to mount " + obbFilePath);

        storageManager.mountObb(obbFilePath, null, obbStateChangeListener);

        try {
            while (!storageManager.isObbMounted(obbFilePath)) {
                Thread.sleep(500);
                Log.d("ConsoDroid", "obb is still not mounted");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setObbPath(storageManager.getMountedObbPath(obbFilePath));
        Log.d("ConsoDroid", "obb is now mounted to " + mountedObbPath);
    }

    public void setObbPath(String path) {
        mountedObbPath = path;
    }

    public void unmountObb() {
        final StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
        String obbPath = Environment.getExternalStorageDirectory() + "/Android/obb";
        final String obbFilePath = obbPath + "/hu.hendricha.consodroid/consodroid.obb";

        if (storageManager.isObbMounted(obbFilePath)) {
            OnObbStateChangeListener listener = new OnObbStateChangeListener() {
                @Override
                public void onObbStateChange(final String path, int state) {
                    super.onObbStateChange(path, state);
                    Log.d("ConsoDroidOBB", "State change: path: " + path + "; state: " + state);
                }
            };

            storageManager.unmountObb(obbFilePath, true, listener);
        }
    }

    public void updateIpAddress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView ipValue = (TextView)findViewById(R.id.ip_value);
                ipValue.setText(IpUtil.getIdealIPAddress());
            }
        });
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
        if (nodeProcess != null) {
            nodeProcess.destroy();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
            String asset = parentDir.getAbsolutePath().replace(Environment.getExternalStorageDirectory().getAbsolutePath() + obbPathOnExternalStorage, "") + "/" + path;
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

    /**
     * Switch click listener, starts/stops node instance on toggle
     *
     * @param view
     * The switch
     */
    public void onSwitchClicked(final View view) {
        boolean on = ((Switch) view).isChecked();
        final TextView title = (TextView)findViewById(R.id.consodroid_switch_title);

        if (on) {
            try {
                nodeProcess = Runtime.getRuntime().exec(mountedObbPath + "/node run.js", new String[0], new File(mountedObbPath));
                Log.d("Consodroid", "Started node");
                title.setText("ConsoDroid is running:");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Consodroid", "Waiting for node exit on a seperate thread");
                        try {
                            nodeProcess.waitFor();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("ConsoDroid", "node exited");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((Switch) view).setChecked(false);
                                title.setText("ConsoDroid is not running:");
                            }
                        });

                    }
                }).start();
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
