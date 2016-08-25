package com.log999.task;

@FunctionalInterface
public interface AbortableTask {

    void run(Aborter aborter) throws Exception;

}
