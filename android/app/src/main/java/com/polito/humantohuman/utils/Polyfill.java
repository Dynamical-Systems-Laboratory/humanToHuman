package com.polito.humantohuman.utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class Polyfill {

    private Polyfill() {}

    public interface Supplier<T> {
        T get();
    }

    public interface Consumer<T> {
        void accept(T t);
    }

    public static class CountdownExecutor {

        public final AtomicInteger countdown;
        public final Runnable executable;

        public CountdownExecutor(int countdown, Runnable executable) {
            this.countdown = new AtomicInteger(countdown);
            this.executable = executable;
        }

        public void decrement() {
            if (countdown.decrementAndGet() == 0) {
                executable.run();
            }
        }
    }
}
