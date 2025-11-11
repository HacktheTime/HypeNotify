package de.hype.hypenotify.app.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import de.hype.hypenotify.app.core.interfaces.MiniCore

open class HypeNotifyService<EXTENDING_CLASS : HypeNotifyService<EXTENDING_CLASS>> : Service() {
    open var core: MiniCore? = null
        protected set
    protected var context: Context? = null

    override fun onBind(intent: Intent): IBinder {
        return HypeNotifyServiceBinder(this as EXTENDING_CLASS)
    }


    inner class HypeNotifyServiceBinder(val service: EXTENDING_CLASS) : Binder() {
    }
}
