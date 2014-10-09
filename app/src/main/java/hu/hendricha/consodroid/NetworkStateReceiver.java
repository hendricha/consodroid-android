package hu.hendricha.consodroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
    Consodroid activity = null;
    void setMainActivityHandler(Consodroid activity) {
        this.activity=activity;
    }

    public void onReceive(Context context, Intent intent) {
        Log.d("ConsoDroid", "Network connectivity change, updating ip address");
        activity.updateIpAddress();
    }
}