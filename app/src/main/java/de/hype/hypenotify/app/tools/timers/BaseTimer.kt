package de.hype.hypenotify.app.tools.timers

import de.hype.hypenotify.app.ServerUtils
import de.hype.hypenotify.app.core.interfaces.MiniCore
import java.time.Instant
import java.util.*

/**
 * Abstract Base Timer
 */
abstract class BaseTimer @JvmOverloads constructor(
    var clientId: UUID?,
    serverId: UUID?,
    var time: Instant?,
    var message: String?,
    hasSleepButton: Boolean = true, //    protected boolean hasSleepButton;
    var isDeactivated: Boolean = false
) {
    var serverId: UUID?
        protected set

    init {
        //        this.hasSleepButton = hasSleepButton;
        this.isDeactivated = isDeactivated
        this.serverId = serverId
    }

    /**
     * checks whether the timer is disabled and if its condition ([.wouldRing] ]}) is met.
     */
    fun shouldRing(core: MiniCore?): Boolean {
        return !this.isDeactivated && wouldRing(core)
    }

    /**
     * whether the timer should ring. you may use blocking code or throw exceptions. if you do not return a cancel within the time limit (usually ~1 Minute) the timer ring anyway showing that problem.
     */
    abstract fun wouldRing(core: MiniCore?): Boolean

    fun deactivate() {
        isDeactivated = true
    }

    /**
     * @param core core object to use code with.
     * use the [.onInitCustom] for custom code to run. the code in this methods executes it first and then its own code.
     */
    fun onInit(core: MiniCore?) {
        onInitCustom(core)
        ServerUtils.uploadTimer(core, this)
    }

    fun onInitCustom(core: MiniCore?) {
    }
}
