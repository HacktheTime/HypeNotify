package de.hype.hypenotify.app

@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigField(val description: String = "", val allowNull: Boolean = false)