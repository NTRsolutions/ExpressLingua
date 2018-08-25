package com.neosolusi.expresslingua;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static AppExecutors mInstance;
    private final Executor mainThread;
    private final Executor networkIO;
    private final Executor diskIO;

    private AppExecutors(Executor mainThread, Executor networkIO, Executor diskIO) {
        this.mainThread = mainThread;
        this.networkIO = networkIO;
        this.diskIO = diskIO;
    }

    public static AppExecutors getInstance() {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new AppExecutors(new MainThreadExecutor(), Executors.newFixedThreadPool(4), Executors.newSingleThreadExecutor());
            }
        }

        return mInstance;
    }

    public Executor mainThread() {
        return this.mainThread;
    }

    public Executor networkIO() {
        return this.networkIO;
    }

    public Executor diskIO() {
        return this.diskIO;
    }

    private static class MainThreadExecutor implements Executor {

        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

}
