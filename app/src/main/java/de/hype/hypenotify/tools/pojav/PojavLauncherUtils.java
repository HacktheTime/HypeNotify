// File: src/main/java/com/example/otherapp/PojavLauncherUtils.java
package de.hype.hypenotify.tools.pojav;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import de.hype.hypenotify.core.interfaces.MiniCore;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

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

    public static Intent launchGameBaseIntent(String profileId, String userDetail) {
        Intent launchIntent = new Intent("net.kdt.pojavlaunch.action.START_PROFILE");
        launchIntent.putExtra("profile_id", profileId);
        launchIntent.putExtra("launch_user", userDetail);
        launchIntent.setComponent(new ComponentName("net.kdt.pojavlaunch.debug", "net.kdt.pojavlaunch.api.StartMinecraftActivity"));
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launchIntent;
    }

    public static void launchToHub(MiniCore core) {
        Socket socket = null;
        int tryCount = 0;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 64987);
            } catch (IOException ignored) {
                if (tryCount == 0) {
                    core.context().startActivity(launchGameBaseIntent(null, null));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored2) {
                }
            }
            if (tryCount > 120)
                throw new IllegalStateException("Something went wrong. Could not connect to Bingo Net Socket Addon. Timeout after 60 Seconds.");
            tryCount++;
        }
        try (OutputStream outputStream = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(outputStream, true)) {
            writer.println("GoToIslandAddonPacket.{\"island\":\"HUB\",\"apiVersionMin\":1,\"apiVersionMax\":1}");
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}