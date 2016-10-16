package com.log999.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.log999.deprecated.chunkloader.LogChunkLoader;
import com.log999.deprecated.logchunk.internal.LogChunkLoaderImpl;
import com.log999.markup.MarkupMemory;
import com.log999.markup.MarkupMemoryImpl;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class Log999ModuleTest {

    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new Log999Module());
    }

    @Test
    public void checkModuleProducesLogChunkLoaderImpl() throws Exception {
        LogChunkLoader loader = injector.getInstance(LogChunkLoader.class);
        assertThat(loader, instanceOf(LogChunkLoaderImpl.class));
    }

    @Test
    public void checkMarkupMemoryImpl() throws Exception {
        MarkupMemory loader = injector.getInstance(MarkupMemory.class);
        assertThat(loader, instanceOf(MarkupMemoryImpl.class));
    }
}
