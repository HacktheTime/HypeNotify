// File: src/main/java/com/example/otherapp/DataResultReceiver.java
package de.hype.hypenotify.tools.pojav;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class DataResultReceiver extends ResultReceiver {
    public interface Callback {
        void onDataReceived(String profiles, String accounts);
    }

    private Callback callback;

    public DataResultReceiver(Handler handler, Callback callback) {
        super(handler);
        this.callback = callback;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        String profiles = resultData.getString("profiles");
        String accounts = resultData.getString("accounts");
        if (callback != null) {
            callback.onDataReceived(profiles, accounts);
        }
    }
}