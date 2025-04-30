package de.hype.hypenotify.app.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class HypeNotifyServiceConnection implements ServiceConnection {
    private List<ServiceFutureWrapper> futures = new ArrayList<>();
    private ConcurrentMap<String, HypeNotifyService<?>> connectedServices = new ConcurrentHashMap<>();

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        HypeNotifyService.HypeNotifyServiceBinder binder = (HypeNotifyService.HypeNotifyServiceBinder) iBinder;
        HypeNotifyService<?> service = binder.getService();
        connectedServices.put(service.getClass().getSimpleName(), service);
        serviceChangedFutureCheck(service.getClass().getSimpleName(), service);
    }

    public <Service extends HypeNotifyService<Service>> CompletableFuture<Service> getService(Class<Service> serviceClass) {
        String className = serviceClass.getSimpleName();
        CompletableFuture<Service> future = new CompletableFuture<>();

        HypeNotifyService<?> existingService = connectedServices.get(className);
        if (existingService != null) {
            @SuppressWarnings("unchecked")
            Service castedService = (Service) existingService;
            future.complete(castedService);
        } else {
            futures.add(new ServiceFutureWrapper(className, future));
        }

        return future;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        String className = name.getClassName();
        connectedServices.remove(className);
        serviceChangedFutureCheck(className, null);
    }

    private void serviceChangedFutureCheck(String className, HypeNotifyService<?> service) {
        for (int i = 0; i < futures.size(); i++) {
            ServiceFutureWrapper wrapper = futures.get(i);
            if (wrapper.future.isDone()) {
                futures.remove(i);
                i--;
            } else {
                if (wrapper.className.equals(className)) {
                    @SuppressWarnings("unchecked")
                    CompletableFuture<HypeNotifyService<?>> castedFuture = (CompletableFuture<HypeNotifyService<?>>) wrapper.future;
                    castedFuture.complete(service);
                    futures.remove(i);
                    i--;
                }
            }
        }
        serviceChange(className, service);
    }

    /**
     * @param serviceName the class name of the service.
     * @param service the service instance.
     */
    protected abstract void serviceChange(String serviceName, HypeNotifyService<?> service);

    private static class ServiceFutureWrapper {
        String className;
        CompletableFuture<? extends HypeNotifyService<?>> future;

        ServiceFutureWrapper(String className, CompletableFuture<? extends HypeNotifyService<?>> future) {
            this.className = className;
            this.future = future;
        }
    }
}