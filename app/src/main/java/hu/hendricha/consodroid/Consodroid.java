package hu.hendricha.consodroid;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Consodroid extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consodroid);

        Log.d("ConsoDroid", "minden előtt");
        TextView ipValue = (TextView)findViewById(R.id.ip_value);
        ipValue.setText(IpUtil.getIdealIPAddress());

        this.copyAsset(this.getFilesDir(), "node");
        try {
            Process nativeApp = Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/hu.hendricha.consodroid/files/node/node");
            nativeApp.waitFor();

            nativeApp = Runtime.getRuntime().exec("/data/data/hu.hendricha.consodroid/files/node/node subdir/hello.js", new String[0], new File("/data/data/hu.hendricha.consodroid/files/node"));
           // nativeApp.waitFor();

            Log.d("Consodroid", "Started node");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.consodroid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            Log.e("Consodroid", e.getMessage());
        }
    }
}
