package com.blinglog.poc.control.internal;

import com.log999.display.api.LogFileDisplayRow;

public interface FormattingFunction {

    void apply(int start, int end, LogFileDisplayRow line);

}
