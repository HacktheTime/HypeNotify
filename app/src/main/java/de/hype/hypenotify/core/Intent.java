package de.hype.hypenotify.core;

import de.hype.hypenotify.MainActivity;
import de.hype.hypenotify.core.interfaces.Core;

public interface Intent {
    void handleIntentInternal(android.content.Intent intent, Core core, MainActivity context);

    String intentId();
}
