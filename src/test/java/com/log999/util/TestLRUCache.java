package com.log999.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TestLRUCache {

    @Mock
    private Consumer<String> consumer;

    @InjectMocks
    private LRUCache<String,String> cache = new LRUCache<>(5, consumer);

    @Test
    public void testCacheKeepsEntrysWhenLessThanSize() throws Exception {
        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value4");

        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value4");

        // Then
        assertThat(cache.size(), equalTo(4));
        verify(consumer, never()).accept(anyString());
    }

    @Test
    public void testCacheKeepsEntrysWhenEqualSize() throws Exception {
        // When
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value4");
        cache.put("key5", "value5");

        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value4");
        cache.put("key5", "value5");

        // Then
        assertThat(cache.size(), equalTo(5));
        verify(consumer, never()).accept(anyString());
    }

    @Test
    public void testCacheDiscardsLeastRecentlyUsedEntrysWhenExceedsSize() throws Exception {
        // Given
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        cache.put("key4", "value4");
        cache.put("key5", "value5");
        // When
        cache.put("key6", "value6");
        // Then
        assertThat(cache.size(), equalTo(5));
        verify(consumer, Mockito.times(1)).accept("key1");
    }
}
