package de.hype.hypenotify.app.core.interfaces;

import de.hype.hypenotify.app.MainActivity;

public interface Core extends MiniCore {
    @Override
    MainActivity context();

    void onDestroy();
}
