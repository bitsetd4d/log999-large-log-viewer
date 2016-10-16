package com.log999.guice;

import com.google.inject.AbstractModule;
import com.log999.deprecated.chunkloader.LogChunkLoader;
import com.log999.deprecated.logchunk.internal.LogChunkLoaderImpl;
import com.log999.markup.MarkupMemory;
import com.log999.markup.MarkupMemoryImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Log999Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(ScheduledExecutorService.class).toInstance(Executors.newScheduledThreadPool(1));
        bind(LogChunkLoader.class).to(LogChunkLoaderImpl.class);
        bind(MarkupMemory.class).to(MarkupMemoryImpl.class);
    }
}
