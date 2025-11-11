package de.hype.hypenotify.app

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ExecutionService(threadPoolSize: Int) {
    var provider: ScheduledExecutorService

    init {
        provider = Executors.newScheduledThreadPool(threadPoolSize)
        if (provider is ScheduledThreadPoolExecutor) {
            val exec = provider as ScheduledThreadPoolExecutor
            exec.setRemoveOnCancelPolicy(true)
            exec.setKeepAliveTime(60, TimeUnit.SECONDS)
            exec.allowCoreThreadTimeOut(true) // erlaubt, dass Threads nach Inaktivität beendet werden
        }
    }

    private fun handleError(e: Throwable) {
        e.printStackTrace()
    }

    fun schedule(runnable: Runnable, delay: Long, timeUnit: TimeUnit): ScheduledFuture<*> {
        return provider.schedule(Runnable {
            try {
                runnable.run()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        }, delay, timeUnit)
    }

    fun <V> schedule(callable: Callable<V?>, delay: Long, timeUnit: TimeUnit): ScheduledFuture<V?> {
        return provider.schedule<V?>(Callable {
            try {
                return@schedule callable.call()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        }, delay, timeUnit)
    }

    fun scheduleAtFixedRate(runnable: Runnable, initialDelay: Long, delayBetween: Long, timeUnit: TimeUnit): ScheduledFuture<*> {
        return provider.scheduleWithFixedDelay(Runnable {
            try {
                runnable.run()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        }, initialDelay, delayBetween, timeUnit)
    }

    fun scheduleWithFixedDelay(runnable: Runnable, initialDelay: Long, betweenDelay: Long, timeUnit: TimeUnit): ScheduledFuture<*> {
        return provider.scheduleWithFixedDelay(Runnable {
            try {
                runnable.run()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        }, initialDelay, betweenDelay, timeUnit)
    }

    fun shutdown() {
        provider.shutdown()
    }

    fun shutdownNow(): MutableList<Runnable?> {
        return provider.shutdownNow()
    }

    val isShutdown: Boolean
        get() = provider.isShutdown()

    val isTerminated: Boolean
        get() = provider.isTerminated()

    @Throws(InterruptedException::class)
    fun awaitTermination(l: Long, timeUnit: TimeUnit): Boolean {
        return provider.awaitTermination(l, timeUnit)
    }

    fun <T> submit(callable: Callable<T?>): Future<T?> {
        return provider.submit<T?>(Callable {
            try {
                return@submit callable.call()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        })
    }

    fun <T> submit(runnable: Runnable, task: T?): Future<T?> {
        return provider.submit<T?>(Callable {
            try {
                runnable.run()
                return@submit task
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        })
    }

    fun submit(runnable: Runnable): Future<*> {
        return provider.submit(Runnable {
            try {
                runnable.run()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        })
    }

    fun execute(runnable: Runnable) {
        provider.execute(Runnable {
            try {
                runnable.run()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        })
    }

    @Throws(InterruptedException::class)
    fun <T> invokeAll(collection: MutableCollection<out Callable<T?>?>): MutableList<Future<T?>?> {
        return provider.invokeAll<T?>(collection.stream().map<Callable<T?>?> { callable: Callable<T?>? ->
            Callable {
                try {
                    return@Callable callable!!.call()
                } catch (e: Throwable) {
                    handleError(e)
                    throw e
                }
            }
        }.toList())
    }

    @Throws(InterruptedException::class)
    fun <T> invokeAll(collection: MutableCollection<out Callable<T?>?>, l: Long, timeUnit: TimeUnit): MutableList<Future<T?>?> {
        return provider.invokeAll<T?>(collection.stream().map<Callable<T?>?> { callable: Callable<T?>? ->
            Callable {
                try {
                    return@Callable callable!!.call()
                } catch (e: Throwable) {
                    handleError(e)
                    throw e
                }
            }
        }.toList(), l, timeUnit)
    }

    fun scheduleAt(runnable: Runnable, instant: Instant?): ScheduledFuture<*> {
        return provider.schedule(Runnable {
            try {
                runnable.run()
            } catch (e: Throwable) {
                handleError(e)
                throw e
            }
        }, Instant.now().until(instant, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS)
    }
}
