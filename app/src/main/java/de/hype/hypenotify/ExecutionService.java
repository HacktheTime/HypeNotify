package de.hype.hypenotify;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ExecutionService {
    ScheduledExecutorService provider;

    public ExecutionService(int threadPoolSize) {
        provider = Executors.newScheduledThreadPool(threadPoolSize);
    }

    private void handleError(Throwable e) {
        e.printStackTrace();
    }

    @NotNull
    public ScheduledFuture<?> schedule(@NotNull Runnable runnable, long delay, @NotNull TimeUnit timeUnit) {
        return provider.schedule(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        }, delay, timeUnit);
    }

    @NotNull
    public <V> ScheduledFuture<V> schedule(@NotNull Callable<V> callable, long delay, @NotNull TimeUnit timeUnit) {
        return provider.schedule(() -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        }, delay, timeUnit);
    }

    @NotNull
    public ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable runnable, long initialDelay, long delayBetween, @NotNull TimeUnit timeUnit) {
        return provider.scheduleWithFixedDelay(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        }, initialDelay, delayBetween, timeUnit);
    }

    @NotNull
    public ScheduledFuture<?> scheduleWithFixedDelay(@NotNull Runnable runnable, long initialDelay, long betweenDelay, @NotNull TimeUnit timeUnit) {
        return provider.scheduleWithFixedDelay(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        }, initialDelay, betweenDelay, timeUnit);
    }

    public void shutdown() {
        provider.shutdown();
    }

    @NotNull
    public List<Runnable> shutdownNow() {
        return provider.shutdownNow();
    }

    public boolean isShutdown() {
        return provider.isShutdown();
    }

    public boolean isTerminated() {
        return provider.isTerminated();
    }

    public boolean awaitTermination(long l, @NotNull TimeUnit timeUnit) throws InterruptedException {
        return provider.awaitTermination(l, timeUnit);
    }

    @NotNull
    public <T> Future<T> submit(@NotNull Callable<T> callable) {
        return provider.submit(() -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        });
    }

    @NotNull
    public <T> Future<T> submit(@NotNull Runnable runnable, T task) {
        return provider.submit(() -> {
            try {
                runnable.run();
                return task;
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        });
    }

    @NotNull
    public Future<?> submit(@NotNull Runnable runnable) {
        return provider.submit(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        });
    }

    public void execute(@NotNull Runnable runnable) {
        provider.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        });
    }

    @NotNull
    public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> collection) throws InterruptedException {
        return provider.invokeAll(collection.stream().map(callable -> (Callable<T>) () -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        }).toList());
    }

    @NotNull
    public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> collection, long l, @NotNull TimeUnit timeUnit) throws InterruptedException {
        return provider.invokeAll(collection.stream().map(callable -> (Callable<T>) () -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        }).toList(), l, timeUnit);
    }

    public ScheduledFuture<?> scheduleAt(Runnable runnable, Instant instant) {
        return provider.schedule(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                handleError(e);
                throw e;
            }
        }, Instant.now().until(instant, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
    }
}
