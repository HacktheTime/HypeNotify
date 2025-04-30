package de.hype.hypenotify.app.core;

import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.app.core.interfaces.Core;

public interface Intent {
    void handleIntentInternal(android.content.Intent intent, Core core, MainActivity context);

    String intentId();
}
