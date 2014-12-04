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
    protected int eventType;

    public AbstractObserver(Activity activity, String folderName, int event) {
        this.activity = activity;
        this.folderName = folderName;
        this.eventType = event;
        this.observe();
    }

    public void observe() {
        final File dir = new File(activity.getFilesDir(), folderName);
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            deleteFiles();
        }

        fileObserver = new FileObserver("/data/data/hu.hendricha.consodroid/files/" + folderName) {
            @Override
            public void onEvent(int event, final String fileName) {
                Log.d("ConsoDroid", "FileObserver event: " + fileName);

                if (event == eventType) {
                    Log.d("ConsoDroid", "Observed event in file: " + folderName + '/' + fileName);
                    manageEvent(dir, fileName);
                }
            }
        };
        fileObserver.startWatching();
        Log.d("ConsoDroid", "FileObserver started watching: " + dir.getAbsolutePath());
    }

    public void stopObserving() {
        fileObserver.stopWatching();
    }

    abstract protected void manageEvent(final File dir, final String fileName);

    private void deleteFiles() {
        File file = new File(activity.getFilesDir(), folderName);
        String[] myFiles;

        myFiles = file.list();
        if (myFiles == null) {
            return;
        }

        Log.d("ConsoDroid", "Removing old " + folderName + " control files");
        for (int i=0; i<myFiles.length; i++) {
            new File(file, myFiles[i]).delete();
        }
    }
}
