package com.log999.guice;

import com.google.inject.AbstractModule;
import com.log999.logfile.deprecated.chunkloader.LogChunkLoader;
import com.log999.logchunk.internal.LogChunkLoaderImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Log999Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(1));
        bind(LogChunkLoader.class).to(LogChunkLoaderImpl.class);
    }
}
