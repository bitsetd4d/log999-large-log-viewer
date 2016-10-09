package com.log999.deprecated.chunkloader;

import com.log999.displaychunk.DisplayableLogChunk;
import com.log999.deprecated.logchunk.internal.LogChunkLoaderImpl;
import com.log999.util.MemoryCalculator;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class LogChunkLoaderTest {

    private LogChunkLoaderImpl loader;
    final private int maxBytesPerChunk = 200;

    @Before
    public void setUp() throws Exception {
        loader = new LogChunkLoaderImpl(maxBytesPerChunk);
    }

    @Test
    public void addedLinesAreRetrieved() throws Exception {
        // Given
        String toAdd1 = "ABCDEFG1";
        String toAdd2 = "ABCDEFG2";
        String toAdd3 = "ABCDEFG3";
        // When
        loader.acceptLine(toAdd1);
        loader.acceptLine(toAdd2);
        loader.acceptLine(toAdd3);
        loader.fullyLoaded();
        // Then
        LogChunks logChunks = loader.getLogChunks();
        DisplayableLogChunk chunk1 = logChunks.logChunkForDisplayRow(0);
        String[] rows = chunk1.getRealRows(3, 0);
        assertThat(rows, arrayWithSize(3));
        assertThat(rows[0], equalTo(toAdd1));
        assertThat(rows[1], equalTo(toAdd2));
        assertThat(rows[2], equalTo(toAdd3));
    }

    @Test
    public void smallLinesFillSingleChunk() throws Exception {
        // Given
        String toAdd = "X";
        assertThat(MemoryCalculator.bytesFor(toAdd), lessThan(maxBytesPerChunk / 4));
        // When
        loader.acceptLine(toAdd);
        loader.acceptLine(toAdd);
        loader.acceptLine(toAdd);
        loader.fullyLoaded();
        // Then
        LogChunks logChunks = loader.getLogChunks();
        DisplayableLogChunk chunk1 = logChunks.logChunkForDisplayRow(0);
        DisplayableLogChunk chunk2 = logChunks.logChunkForDisplayRow(1);
        DisplayableLogChunk chunk3 = logChunks.logChunkForDisplayRow(2);
        assertTrue(chunk1 == chunk2);
        assertTrue(chunk2 == chunk3);
    }

    @Test
    public void bigLinesFillMultipleChunks() throws Exception {
        // Given
        String toAdd = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
        assertThat(MemoryCalculator.bytesFor(toAdd), greaterThan(maxBytesPerChunk / 2));
        assertThat(MemoryCalculator.bytesFor(toAdd), lessThanOrEqualTo(maxBytesPerChunk));
        // When
        loader.acceptLine(toAdd);
        loader.acceptLine(toAdd);
        loader.acceptLine(toAdd);
        loader.fullyLoaded();
        // Then
        LogChunks logChunks = loader.getLogChunks();
        DisplayableLogChunk chunk1 = logChunks.logChunkForDisplayRow(0);
        DisplayableLogChunk chunk2 = logChunks.logChunkForDisplayRow(1);
        DisplayableLogChunk chunk3 = logChunks.logChunkForDisplayRow(2);
        assertTrue(chunk1 != chunk2);
        assertTrue(chunk2 != chunk3);
        assertTrue(chunk3 != chunk1);
    }
}
