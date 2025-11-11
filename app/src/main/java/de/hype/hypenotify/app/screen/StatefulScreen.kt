package de.hype.hypenotify.app.screen

import android.os.Bundle

/**
 * Interface für Screens, die ihren Zustand speichern und wiederherstellen können
 */
interface StatefulScreen {
    /**
     * Speichert den aktuellen Zustand des Screens in das Bundle
     *
     * @param state Bundle zum Speichern der Daten
     */
    fun saveState(state: Bundle?)

    /**
     * Stellt den Zustand des Screens aus dem Bundle wieder her
     *
     * @param state Bundle mit den gespeicherten Daten
     */
    fun restoreState(state: Bundle?)

    /**
     * Gibt eine eindeutige ID für diesen Screen zurück
     *
     * @return Eindeutige Screen-ID
     */
    val screenId: String?
}
