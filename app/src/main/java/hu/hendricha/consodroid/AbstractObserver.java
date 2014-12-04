package hu.hendricha.consodroid;

import android.app.Activity;
import android.os.FileObserver;
import android.util.Log;
import java.io.File;

/**
 * Created by cruelangel on 2014.12.04..
 */
public abstract class AbstractObserver {
    protected final Activity activity;
    private FileObserver fileObserver;
    protected final String folderName;

    public AbstractObserver(Activity activity, String folderName) {
        this.activity = activity;
        this.folderName = folderName;
        this.observe();
    }

    public void observe() {
        final File dir = new File(activity.getFilesDir(), folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        fileObserver = new FileObserver("/data/data/hu.hendricha.consodroid/files/" + folderName) {
            @Override
            public void onEvent(int event, final String fileName) {
                Log.d("ConsoDroid", "FileObserver event: " + fileName);

                if (event == FileObserver.CREATE) {
                    Log.d("ConsoDroid", "Found new observed file: " + fileName);
                    manageCreateEvent(dir, fileName);
                }
            }
        };
        fileObserver.startWatching();
        Log.d("ConsoDroid", "FileObserver started watching: " + dir.getAbsolutePath());
    }

    public void stopObserving() {
        fileObserver.stopWatching();
    }

    abstract protected void manageCreateEvent(final File dir, final String fileName);
}
