package com.blinglog.poc.control.internal;

import com.blinglog.poc.file.LogFileDisplayRow;

public interface FormattingFunction {

    void apply(int start, int end, LogFileDisplayRow line);

}
