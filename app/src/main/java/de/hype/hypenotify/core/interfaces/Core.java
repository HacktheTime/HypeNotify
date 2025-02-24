package de.hype.hypenotify.core.interfaces;

import de.hype.hypenotify.MainActivity;

public interface Core extends MiniCore {
    @Override
    MainActivity context();

    void onDestroy();
}
