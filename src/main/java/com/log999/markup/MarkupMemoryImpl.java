package com.log999.markup;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MarkupMemoryImpl implements MarkupMemory {

    private static Logger logger = LoggerFactory.getLogger(MarkupMemoryImpl.class);
    private Map<Long,Boolean> markedMap = new HashMap<>();
    private Map<Long,LineMarkup> markupMap = new HashMap<>();

    @Override
    public void setMarked(long from, long line, boolean marked) {
        for (long i=from; i<=line; i++) {
            if (marked) {
                markedMap.put(i,true);
            } else {
                markedMap.remove(i);
            }
        }
    }

    @Override
    public boolean isMarked(long line) {
        return markedMap.getOrDefault(line,Boolean.FALSE) == true;
    }

    @Override
    public void setBold(long lineNumber, int start, int end) {
        LineMarkup lineMarkup = getFormatting(lineNumber);
        lineMarkup.markBold(start,end);
    }

    @Override
    public void setBackground(long lineNumber, int start, int end, Color bg) {
        LineMarkup lineMarkup = getFormatting(lineNumber);
        lineMarkup.markBackground(start, end, bg);
    }

    @Override
    public void setForeground(long lineNumber, int start, int end, Color bg) {
        LineMarkup lineMarkup = getFormatting(lineNumber);
        lineMarkup.markForeground(start, end, bg);
    }

    @Override
    public LineMarkup getFormatting(long lineNumber) {
        LineMarkup lineMarkup = markupMap.computeIfAbsent(lineNumber, k -> new LineMarkup());
        return lineMarkup;
    }

}
