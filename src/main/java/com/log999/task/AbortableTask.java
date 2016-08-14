package com.log999.task;

public interface AbortableTask {

    void run(Aborter aborter) throws Exception;

}
