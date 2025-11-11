// File: src/main/java/com/example/otherapp/DataResultReceiver.java
package de.hype.hypenotify.app.tools.pojav

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class DataResultReceiver(handler: Handler?, private val callback: Callback?) : ResultReceiver(handler) {
    interface Callback {
        fun onDataReceived(profiles: String?, accounts: String?)
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
        val profiles = resultData.getString("profiles")
        val accounts = resultData.getString("accounts")
        if (callback != null) {
            callback.onDataReceived(profiles, accounts)
        }
    }
}