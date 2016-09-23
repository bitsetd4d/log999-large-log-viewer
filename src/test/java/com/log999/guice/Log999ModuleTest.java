package com.log999.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.log999.logchunk.LogChunkLoader;
import com.log999.logchunk.internal.LogChunkLoaderImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public class Log999ModuleTest {

    @Test
    public void checkModuleProducesLogChunkLoaderImpl() throws Exception {
        Injector injector = Guice.createInjector(new Log999Module());
        LogChunkLoader loader = injector.getInstance(LogChunkLoader.class);
        assertThat(loader, instanceOf(LogChunkLoaderImpl.class));
    }
}
