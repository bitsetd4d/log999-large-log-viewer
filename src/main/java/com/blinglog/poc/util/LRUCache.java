package com.blinglog.poc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static Logger logger = LoggerFactory.getLogger(LRUCache.class);

    private int cacheSize;

    private Consumer<K> consumer;

    public LRUCache(int cacheSize,Consumer<K> consumer) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
        this.consumer = consumer;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        boolean evict = size() >= cacheSize;
        if (evict) {
            consumer.accept(eldest.getKey());
        }
        return evict;
    }

}
