package de.hype.hypenotify.app.tools.timers

import de.hype.hypenotify.app.core.interfaces.MiniCore
import java.time.Instant
import java.util.*

/**
 * AlwaysRingingTimer - Rings at a set time, no conditions.
 */
class AlwaysRingingTimer(id: UUID?, time: Instant?, message: String?) : BaseTimer(id, null, time, message) {
    override fun wouldRing(core: MiniCore?): Boolean {
        return true
    }
}
