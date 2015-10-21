package com.blinglog.poc.task;

public interface AbortableTask {

    void run(Aborter aborter) throws Exception;

}
