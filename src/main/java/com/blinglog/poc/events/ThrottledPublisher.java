package com.blinglog.poc.events;

import java.util.function.Consumer;

public interface ThrottledPublisher<T> {

    void publish(T value);
    T get();

    void onPublishNow(Consumer<T> consumer);

}
