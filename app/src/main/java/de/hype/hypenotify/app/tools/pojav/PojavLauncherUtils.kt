// File: src/main/java/com/example/otherapp/PojavLauncherUtils.java
package de.hype.hypenotify.app.tools.pojav

import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import de.hype.hypenotify.app.core.interfaces.MiniCore
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

class PojavLauncherUtils : DataResultReceiver.Callback {
    private fun fetchData() {
        val receiver = DataResultReceiver(Handler(), this)
        val intent = Intent()
        intent.setAction("net.kdt.pojavlaunch.pojavlauncher.action.GET_PROFILE_IDS")
        intent.putExtra("result_receiver", receiver)
        // Specify the package name of the application hosting the services.
        intent.setPackage("net.kdt.pojavlaunch.debug")
    }

    override fun onDataReceived(profiles: String?, accounts: String?) {
//        Toast.makeText(this, "Profiles: " + profiles + "\nAccounts: " + accounts, Toast.LENGTH_LONG).show();
        // Process the received JSON data as needed.
    }

    companion object {
        fun launchGameBaseIntent(profileId: String?, userDetail: String?): Intent {
            val launchIntent = Intent("net.kdt.pojavlaunch.action.START_PROFILE")
            launchIntent.putExtra("profile_id", profileId)
            launchIntent.putExtra("launch_user", userDetail)
            launchIntent.setComponent(ComponentName("net.kdt.pojavlaunch.debug", "net.kdt.pojavlaunch.api.StartMinecraftActivity"))
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return launchIntent
        }

        fun launchToHub(core: MiniCore) {
            var socket: Socket? = null
            var tryCount = 0
            while (socket == null) {
                try {
                    socket = Socket("localhost", 64987)
                } catch (ignored: IOException) {
                    if (tryCount == 0) {
                        core.context().startActivity(launchGameBaseIntent(null, null))
                    }
                    try {
                        Thread.sleep(1000)
                    } catch (ignored2: InterruptedException) {
                    }
                }
                check(tryCount <= 120) { "Something went wrong. Could not connect to Bingo Net Socket Addon. Timeout after 60 Seconds." }
                tryCount++
            }
            try {
                socket.getOutputStream().use { outputStream ->
                    PrintWriter(outputStream, true).use { writer ->
                        writer.println("GoToIslandAddonPacket.{\"island\":\"HUB\",\"apiVersionMin\":1,\"apiVersionMax\":1}")
                        socket.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}