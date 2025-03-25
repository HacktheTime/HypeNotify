// File: src/main/java/com/example/otherapp/OtherAppLauncher.java
package de.hype.hypenotify.tools.pojav;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import de.hype.hypenotify.core.IntentBuilder;

public class OtherAppLauncher implements DataResultReceiver.Callback {
    private void fetchData() {
        DataResultReceiver receiver = new DataResultReceiver(new Handler(), this);
        Intent intent = new Intent();
        intent.setAction("net.kdt.pojavlaunch.pojavlauncher.action.GET_PROFILE_IDS");
        intent.putExtra("result_receiver", receiver);
        // Specify the package name of the application hosting the services.
        intent.setPackage("net.kdt.pojavlaunch.debug");
    }

    @Override
    public void onDataReceived(String profiles, String accounts) {
//        Toast.makeText(this, "Profiles: " + profiles + "\nAccounts: " + accounts, Toast.LENGTH_LONG).show();
        // Process the received JSON data as needed.
    }

    public static PendingIntent launchGameIntent(Context context, String profileId, String userDetail) {
        IntentBuilder launchIntent = new IntentBuilder(context, "net.kdt.pojavlaunch.action.START_PROFILE", IntentBuilder.PendingType.SERVICE);
        launchIntent.putExtra("profile_id", profileId);
        launchIntent.putExtra("launch_user", userDetail);
        // Specify the package name of the target application.
        launchIntent.setPackage("net.kdt.pojavlaunch.debug");
        launchIntent.setFlags();
        return launchIntent.getAsPending(false);
    }
}