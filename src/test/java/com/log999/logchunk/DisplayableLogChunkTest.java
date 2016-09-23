package com.log999.logchunk;

import com.log999.logchunk.internal.LogChunkImpl;
import com.log999.util.LogFilePosition;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DisplayableLogChunkTest {

    private DisplayableLogChunk chunk;

    @Before
    public void setUp() throws Exception {
        LogChunkImpl logChunk = new LogChunkImpl(1,2000);
        logChunk.acceptLineIfRoom("1234567890");
        logChunk.acceptLineIfRoom("1234567890");
        logChunk.acceptLineIfRoom("1234567890");
        chunk = logChunk;
    }

    @Test
    public void shouldRememberDisplayLineStartIndex() throws Exception {
        // when
        chunk.setDisplayRowStartIndex(99L);
        // then
        assertThat(chunk.getDisplayRowStartIndex(), equalTo(99L));
    }

    @Test
    public void shouldBeLoaded() throws Exception {
        // Given that we've not implemented loading and unloading
        // Then
        assertThat(chunk.isLoaded(), is(true));
    }

    @Test
    public void shouldGetDisplayRows() throws Exception {
        LogFilePosition displayRow = chunk.getDisplayRow(1);
        fail();
        // TODO
        /*
          Change this so that LogChunks are just the physical log lines and have no
          display aspect.
          The Gui should talk to something that uses LogChunks and maps that to
          display row things. A new layer that doesn't need the LogChunks to support them.
          Then the logchunks can just deal with caching/searching etc.
          The GUI layer can deal with mapping multiple display rows to 1 real row.
         */
    }

    @Test
    public void shouldGetHoldingRowsThatMatchDisplayRowSizes() throws Exception {


    }

    @Test
    public void displayRowsShouldWrapCorrectly() throws Exception {


    }

}
