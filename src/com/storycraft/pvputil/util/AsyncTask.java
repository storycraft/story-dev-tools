package com.storycraft.pvputil.util;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class AsyncTask<T> {

    private final static ExecutorService executor;

    static {
        executor = Executors.newCachedThreadPool();
    }

    private AsyncCallable<T> supplier;

    public AsyncTask(AsyncCallable<T> supplier) {
        this.supplier = supplier;
    }

    public CompletableFuture<T> run() {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public T getSync() {
        return supplier.get();
    }

    public static abstract class AsyncCallable<T> implements Supplier<T> {
        public <A>A await(AsyncTask<A> task){
            return task.run().join();
        }
    }
}
