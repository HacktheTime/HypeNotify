package de.hype.hypenotify.app.core

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import de.hype.hypenotify.app.MainActivity
import java.util.List
import java.util.Set

class IntentBuilder {
    val context: Context?
    private val intent: android.content.Intent
    private val whatNeedsToBeSet: MutableSet<ToDefineInBuilder?> =
        HashSet<ToDefineInBuilder?>(Set.of<ToDefineInBuilder?>(*ToDefineInBuilder.entries.toTypedArray()))
    private var intentId: Int = intentIdCounter++
    var pendingType: PendingType? = null

    constructor(context: Context?, action: String?, pendingType: PendingType) {
        this.context = context
        this.pendingType = pendingType
        intent = android.content.Intent(action)
    }

    constructor(context: Context?, intent: Intent) {
        this.context = context
        if (intent is StaticIntents) {
            this.pendingType = PendingType.BROADCAST
            this.intent = android.content.Intent(context, BroadcastIntentWatcher::class.java)
            this.intent.setAction(StaticIntents.Companion.BASE_INTENT_NAME)
        } else {
            this.pendingType = PendingType.ACTIVITY
            this.intent = android.content.Intent(context, MainActivity::class.java)
            this.intent.setAction(DynamicIntents.Companion.DYNAMIC_INTENT)
        }

        this.intent.putExtra("intentId", intent.intentId())
    }

    fun getAsPending(mutable: Boolean): PendingIntent? {
        return getAsPending(mutable, pendingType!!)
    }

    fun getAsPending(mutable: Boolean, pendingType: PendingType): PendingIntent? {
        val intent = this.asIntent
        return when (pendingType) {
            PendingType.SERVICE -> PendingIntent.getService(
                context,
                intentId,
                intent,
                if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
            )
            PendingType.FOREGROUND_SERVICE -> PendingIntent.getForegroundService(
                context,
                intentId,
                intent,
                if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
            )
            PendingType.BROADCAST -> PendingIntent.getBroadcast(
                context,
                intentId,
                intent,
                if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
            )
            PendingType.ACTIVITY -> PendingIntent.getActivity(
                context,
                intentId,
                intent,
                if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
            )
            PendingType.ACTIVITIES -> PendingIntent.getActivities(
                context,
                intentId,
                arrayOf<android.content.Intent?>(intent),
                if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    fun setComponent(packageName: String?, className: String?) {
        intent.setComponent(ComponentName(packageName!!, className!!))
    }

    enum class PendingType {
        ACTIVITY,
        BROADCAST,
        SERVICE,
        FOREGROUND_SERVICE,
        ACTIVITIES
    }

    val asPending: PendingIntent?
        get() = getAsPending(false)


    fun setFlags(vararg flags: IntentFlag?): IntentBuilder {
        return setFlags(Set.of<IntentFlag?>(*flags))
    }

    fun setFlags(flags: MutableCollection<IntentFlag>): IntentBuilder {
        intent.setFlags(0)
        for (flag in flags) {
            flag.addFlags(intent)
        }
        whatNeedsToBeSet.remove(ToDefineInBuilder.FLAGS)
        return this
    }

    val asIntent: Intent
        get() {
            check(whatNeedsToBeSet.isEmpty()) { "You still need to set: " + whatNeedsToBeSet }
            return intent
        }

    fun putExtra(key: String?, value: String?) {
        intent.putExtra(key, value)
    }

    fun putExtra(key: String?, value: Int?) {
        intent.putExtra(key, value)
    }

    fun setCustomID(id: Int) {
        this.intentId = id
    }

    fun setPackage(id: String?) {
        intent.setPackage(id)
    }

    private enum class ToDefineInBuilder {
        FLAGS,
    }

    enum class IntentFlag(val value: Int) {
        FLAG_GRANT_READ_URI_PERMISSION(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION),
        FLAG_GRANT_WRITE_URI_PERMISSION(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
        FLAG_FROM_BACKGROUND(android.content.Intent.FLAG_FROM_BACKGROUND),
        FLAG_DEBUG_LOG_RESOLUTION(android.content.Intent.FLAG_DEBUG_LOG_RESOLUTION),
        FLAG_EXCLUDE_STOPPED_PACKAGES(android.content.Intent.FLAG_EXCLUDE_STOPPED_PACKAGES),
        FLAG_INCLUDE_STOPPED_PACKAGES(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES),
        FLAG_GRANT_PERSISTABLE_URI_PERMISSION(android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),
        FLAG_GRANT_PREFIX_URI_PERMISSION(android.content.Intent.FLAG_GRANT_PREFIX_URI_PERMISSION),
        FLAG_DIRECT_BOOT_AUTO(android.content.Intent.FLAG_DIRECT_BOOT_AUTO),
        FLAG_ACTIVITY_NO_HISTORY(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY),
        FLAG_ACTIVITY_SINGLE_TOP(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP),
        FLAG_ACTIVITY_NEW_TASK(android.content.Intent.FLAG_ACTIVITY_NEW_TASK),
        FLAG_ACTIVITY_MULTIPLE_TASK(android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
        FLAG_ACTIVITY_CLEAR_TOP(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP),
        FLAG_ACTIVITY_FORWARD_RESULT(android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT),
        FLAG_ACTIVITY_PREVIOUS_IS_TOP(android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP),
        FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS(android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS),
        FLAG_ACTIVITY_BROUGHT_TO_FRONT(android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT),
        FLAG_ACTIVITY_RESET_TASK_IF_NEEDED(android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
        FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY(android.content.Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY),
        FLAG_ACTIVITY_NEW_DOCUMENT(android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT),
        FLAG_ACTIVITY_NO_USER_ACTION(android.content.Intent.FLAG_ACTIVITY_NO_USER_ACTION),
        FLAG_ACTIVITY_REORDER_TO_FRONT(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
        FLAG_ACTIVITY_NO_ANIMATION(android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION),
        FLAG_ACTIVITY_CLEAR_TASK(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK),
        FLAG_ACTIVITY_TASK_ON_HOME(android.content.Intent.FLAG_ACTIVITY_TASK_ON_HOME),
        FLAG_ACTIVITY_RETAIN_IN_RECENTS(android.content.Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS),
        FLAG_ACTIVITY_LAUNCH_ADJACENT(android.content.Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT),
        FLAG_ACTIVITY_MATCH_EXTERNAL(android.content.Intent.FLAG_ACTIVITY_MATCH_EXTERNAL),
        FLAG_ACTIVITY_REQUIRE_NON_BROWSER(android.content.Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER),
        FLAG_ACTIVITY_REQUIRE_DEFAULT(android.content.Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT);

        fun addFlags(intent: android.content.Intent) {
            intent.addFlags(value)
        }

        companion object {
            fun getFlags(intent: android.content.Intent): MutableList<IntentFlag?> {
                val flags: MutableList<IntentFlag?> = ArrayList<IntentFlag?>()
                for (flag in IntentFlag.entries) {
                    if ((intent.getFlags() and flag.value) != 0) {
                        flags.add(flag)
                    }
                }
                return flags
            }
        }
    }

    companion object {
        var DEFAULT_FRONT_OR_CREATE: MutableList<IntentFlag?> =
            List.of<IntentFlag?>(IntentFlag.FLAG_ACTIVITY_NEW_TASK, IntentFlag.FLAG_ACTIVITY_CLEAR_TOP)
        var DEFAULT_CREATE_NEW: MutableList<IntentFlag?> =
            List.of<IntentFlag?>(IntentFlag.FLAG_ACTIVITY_NEW_TASK, IntentFlag.FLAG_ACTIVITY_MULTIPLE_TASK)
        private var intentIdCounter = 1
        fun generateId(): Int {
            return intentIdCounter++
        }
    }
}
