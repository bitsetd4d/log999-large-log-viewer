package com.log999.display.api;

import com.log999.loading.api.StreamingLogLineProvider;
import com.log999.markup.MarkupMemory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;

import java.io.IOException;

public interface LogFileDisplay {

    void load(StreamingLogLineProvider logLines) throws IOException;

    MarkupMemory getMarkupMemory();

    void setLineWrapWidth(int width);
    void setRangeOfInterest(int top,int rowsToRequest);

    IntegerProperty maxLineLengthProperty();
    ObjectProperty<LogFilePage> logFilePageProperty();

}
