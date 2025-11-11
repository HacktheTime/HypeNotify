package de.hype.hypenotify.app.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import de.hype.hypenotify.app.services.HypeNotifyService.HypeNotifyServiceBinder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

abstract class HypeNotifyServiceConnection : ServiceConnection {
    private val futures: MutableList<ServiceFutureWrapper> = ArrayList<ServiceFutureWrapper>()
    private val connectedServices: ConcurrentMap<String?, HypeNotifyService<*>?> = ConcurrentHashMap<String, HypeNotifyService<*>>()

    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        val binder: HypeNotifyServiceBinder = iBinder as HypeNotifyServiceBinder
        val service: HypeNotifyService<*> = binder.getService()
        connectedServices.put(service.javaClass.getSimpleName(), service)
        serviceChangedFutureCheck(service.javaClass.getSimpleName(), service)
    }

    fun <Service : HypeNotifyService<Service?>?> getService(serviceClass: Class<Service?>): CompletableFuture<Service?> {
        val className = serviceClass.getSimpleName()
        val future = CompletableFuture<Service?>()

        val existingService = connectedServices.get(className)
        if (existingService != null) {
            val castedService = existingService as Service?
            future.complete(castedService)
        } else {
            futures.add(ServiceFutureWrapper(className, future))
        }

        return future
    }

    override fun onServiceDisconnected(name: ComponentName) {
        val className: String? = name.getClassName()
        connectedServices.remove(className)
        serviceChangedFutureCheck(className, null)
    }

    private fun serviceChangedFutureCheck(className: String?, service: HypeNotifyService<*>?) {
        var i = 0
        while (i < futures.size) {
            val wrapper = futures.get(i)
            if (wrapper.future.isDone()) {
                futures.removeAt(i)
                i--
            } else {
                if (wrapper.className == className) {
                    val castedFuture = wrapper.future as CompletableFuture<HypeNotifyService<*>?>
                    castedFuture.complete(service)
                    futures.removeAt(i)
                    i--
                }
            }
            i++
        }
        serviceChange(className, service)
    }

    /**
     * @param serviceName the class name of the service.
     * @param service the service instance.
     */
    protected abstract fun serviceChange(serviceName: String?, service: HypeNotifyService<*>?)

    private class ServiceFutureWrapper(className: String, future: CompletableFuture<out HypeNotifyService<*>?>) {
        var className: String
        var future: CompletableFuture<out HypeNotifyService<*>?>

        init {
            this.className = className
            this.future = future
        }
    }
}