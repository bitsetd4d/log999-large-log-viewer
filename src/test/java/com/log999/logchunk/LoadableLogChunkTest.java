package com.log999.logchunk;

import com.log999.logchunk.internal.LogChunkImpl;
import com.log999.util.MemoryCalculator;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoadableLogChunkTest {

    static final int MAX_BYTES_IN_CHUNK = 200;

    private LoadableLogChunk logChunk;

    @Before
    public void setUp() throws Exception {
        logChunk = new LogChunkImpl(1, MAX_BYTES_IN_CHUNK);
    }

    @Test
    public void shouldRememberLogLineStartIndex() throws Exception {
        // when
        logChunk.setLogLineStartIndex(22L);
        // then
        assertThat(logChunk.getLogLineStartIndex(), equalTo(22L));
    }

    @Test
    public void onlyAcceptLineIfRoom() throws Exception {
        // Given
        String toAdd = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFG";
        int size = MemoryCalculator.bytesFor(toAdd);
        assertThat(size, lessThanOrEqualTo(MAX_BYTES_IN_CHUNK));
        assertThat(size, greaterThan(MAX_BYTES_IN_CHUNK/2));
        // When
        boolean added = logChunk.acceptLineIfRoom(toAdd);
        // Then
        assertThat(added, is(true));
        assertThat(logChunk.getLines(), hasSize(1));
        // And when
        boolean addedAgain = logChunk.acceptLineIfRoom(toAdd);
        assertThat(addedAgain, is(false));
        assertThat(logChunk.getLines(), hasSize(1));
    }

    @Test
    public void returnBytesInChunk() throws Exception {
        // Given
        String toAdd = "ABCDEFGHIJKL";
        int size = MemoryCalculator.bytesFor(toAdd);
        // When
        boolean added = logChunk.acceptLineIfRoom(toAdd);
        // Then
        assertThat(added, is(true));
        assertThat(logChunk.getBytesInChunk(), equalTo(size));
    }

    @Test
    public void shouldReturnLinesThatAreAcceptedIntoChunk() throws Exception {
        // Given
        List<String> expectedItems = Arrays.asList("a", "b", "c'");
        // When
        expectedItems.forEach(item -> logChunk.acceptLineIfRoom(item));
        // Then
        assertThat(logChunk.getLines(), hasSize(expectedItems.size()));
        assertThat(logChunk.getLines(), equalTo(expectedItems));
    }

}
