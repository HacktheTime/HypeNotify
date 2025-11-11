package de.hype.hypenotify.app.core

import android.content.Intent
import de.hype.hypenotify.app.MainActivity
import de.hype.hypenotify.app.core.interfaces.Core

interface Intent {
    fun handleIntentInternal(intent: Intent?, core: Core?, context: MainActivity?)

    fun intentId(): String?
}
