package de.hype.hypenotify.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import de.hype.hypenotify.Core;

public class HypeNotifyServiceConnection implements ServiceConnection {
        private final Core core;

        public HypeNotifyServiceConnection(Core core) {
            this.core = core;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            HypeNotifyService.HypeNotifyServiceBinder binder = (HypeNotifyService.HypeNotifyServiceBinder) iBinder ;
            HypeNotifyService<?> service = binder.getService();
            service.setCore(core);
            setValue(service.getClass().getSimpleName(), service);
        }

        public <T extends HypeNotifyService<T>> void setValue(String name, HypeNotifyService<T> uncasedService) {
            ServiceAccessor accessor;
            if (name.equals(TimerService.class.getSimpleName())) {
                accessor = (service1, core) -> core.timerService = (TimerService) uncasedService;
            } else {
                throw new IllegalArgumentException("Unknown service: " + name);
            }
            accessor.setValue(uncasedService, core);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            setValue(name.getClassName(), null);
        }

        @FunctionalInterface
        private interface ServiceAccessor {
            void setValue(HypeNotifyService<?> service, Core core);
        }
    }