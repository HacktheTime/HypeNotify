// File: src/main/java/com/example/otherapp/PojavLauncherUtils.java
package de.hype.hypenotify.tools.pojav;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import de.hype.hypenotify.core.IntentBuilder;

public class PojavLauncherUtils implements DataResultReceiver.Callback {
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
        IntentBuilder launchIntent = new IntentBuilder(context, "net.kdt.pojavlaunch.action.START_PROFILE", IntentBuilder.PendingType.ACTIVITY);
        launchIntent.putExtra("profile_id", profileId);
        launchIntent.putExtra("launch_user", userDetail);
        launchIntent.setPackage("net.kdt.pojavlaunch.debug");
        launchIntent.setFlags(IntentBuilder.IntentFlag.FLAG_INCLUDE_STOPPED_PACKAGES);
        return launchIntent.getAsPending(false);
    }

    public static Intent launchGameBaseIntent(String profileId, String userDetail) {
        Intent launchIntent = new Intent("net.kdt.pojavlaunch.action.START_PROFILE");
        launchIntent.putExtra("profile_id", profileId);
        launchIntent.putExtra("launch_user", userDetail);
        launchIntent.setComponent(new ComponentName("net.kdt.pojavlaunch.debug", "net.kdt.pojavlaunch.api.StartMinecraftActivity"));
        return launchIntent;
    }
}