package de.hype.hypenotify.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import de.hype.hypenotify.core.interfaces.MiniCore;

public class HypeNotifyService<EXTENDING_CLASS extends HypeNotifyService<EXTENDING_CLASS>> extends Service {
    protected MiniCore core;
    protected Context context;

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public IBinder onBind(Intent intent) {
        return new HypeNotifyServiceBinder((EXTENDING_CLASS) this);
    }


    public MiniCore getCore() {
        return core;
    }

    public class HypeNotifyServiceBinder extends Binder {
        private final EXTENDING_CLASS service;

        public HypeNotifyServiceBinder(EXTENDING_CLASS service) {
            this.service = service;
        }

        public EXTENDING_CLASS getService() {
            return service;
        }
    }
}
