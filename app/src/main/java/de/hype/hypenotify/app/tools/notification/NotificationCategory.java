package de.hype.hypenotify.app.tools.notification;

public enum NotificationCategory {
    CATEGORY_CALL("call"),

    /**
     * Notification category: map turn-by-turn navigation.
     */
    CATEGORY_NAVIGATION("navigation"),

    /**
     * Notification category: incoming direct message (SMS, instant message, etc.).
     */
    CATEGORY_MESSAGE("msg"),

    /**
     * Notification category: asynchronous bulk message (email).
     */
    CATEGORY_EMAIL("email"),

    /**
     * Notification category: calendar event.
     */
    CATEGORY_EVENT("event"),

    /**
     * Notification category: promotion or advertisement.
     */
    CATEGORY_PROMO("promo"),

    /**
     * Notification category: alarm or timer.
     */
    CATEGORY_ALARM("alarm"),

    /**
     * Notification category: progress of a long-running background operation.
     */
    CATEGORY_PROGRESS("progress"),

    /**
     * Notification category: social network or sharing update.
     */
    CATEGORY_SOCIAL("social"),

    /**
     * Notification category: error in background operation or authentication status.
     */
    CATEGORY_ERROR("err"),

    /**
     * Notification category: media transport control for playback.
     */
    CATEGORY_TRANSPORT("transport"),

    /**
     * Notification category: indication of running background service.
     */
    CATEGORY_SERVICE("service"),

    /**
     * Notification category: a specific, timely recommendation for a single thing.
     * For example, a news app might want to recommend a news story it believes the user will
     * want to read next.
     */
    CATEGORY_RECOMMENDATION("recommendation"),

    /**
     * Notification category: ongoing information about device or contextual status.
     */
    CATEGORY_STATUS("status"),

    /**
     * Notification category: user-scheduled reminder.
     */
    CATEGORY_REMINDER("reminder"),
    /**
     * Notification category: tracking a user's workout.
     */
    CATEGORY_WORKOUT("workout"),

    /**
     * Notification category: temporarily sharing location.
     */
    CATEGORY_LOCATION_SHARING("location_sharing"),

    /**
     * Notification category: running stopwatch.
     */
    CATEGORY_STOPWATCH("stopwatch"),

    /**
     * Notification category: missed call.
     */
    CATEGORY_MISSED_CALL("missed_call"),

    /**
     * Notification category: voicemail.
     */
    CATEGORY_VOICEMAIL("voicemail");

    public final String categoryId;

    NotificationCategory(String categoryId) {
        this.categoryId = categoryId;
    }
}
