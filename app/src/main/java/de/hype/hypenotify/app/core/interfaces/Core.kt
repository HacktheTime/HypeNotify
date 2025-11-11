package de.hype.hypenotify.app.core.interfaces

import de.hype.hypenotify.app.MainActivity

interface Core : MiniCore {
    override fun context(): MainActivity

    fun onDestroy()
}
