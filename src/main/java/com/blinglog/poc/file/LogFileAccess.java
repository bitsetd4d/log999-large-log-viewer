package com.blinglog.poc.file;

import com.blinglog.poc.file.diskbacked.DiskBackedLogFileAccess;
import com.blinglog.poc.file.internal.SimpleFileAccessImpl;
import com.log999.display.api.LogFilePage;
import com.log999.markup.MarkupMemory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;

import java.io.IOException;


public interface LogFileAccess {

    static LogFileAccess newSimpleInstance() { return new SimpleFileAccessImpl(); }
    static LogFileAccess newDiskBackedInstance() { return new DiskBackedLogFileAccess(); }

    void readFile(String fileName) throws IOException;
    LongProperty numberOfLinesProperty();
    IntegerProperty maxLineLengthProperty();
    BooleanProperty fullyIndexedProperty();
    ObjectProperty<LogFilePage> logFilePageProperty();
    MarkupMemory getMarkupMemory();

    // Input
    IntegerProperty lineWrapWidthProperty();

    void setRangeOfInterest(int top,int rowsToRequest);

}
