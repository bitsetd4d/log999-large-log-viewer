package com.blinglog.poc.file.internal;

import java.util.concurrent.*;

/**
 * Index the file
 */
public class FileLineIndexer implements Callable<Void> {

    private String fileName;

    public FileLineIndexer(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}