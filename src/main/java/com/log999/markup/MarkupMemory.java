package com.log999.markup;

import javafx.scene.paint.Color;

public interface MarkupMemory {

    static MarkupMemory newInstance() { return new MarkupMemoryImpl(); }

    void setMarked(long from, long line,boolean marked);
    boolean isMarked(long line);

    void setBold(long lineNumber, int start, int end, boolean bold);
    void setBackground(long lineNumber, int start, int end, Color bg);
    void setForeground(long lineNumber, int start, int end, Color bg);

    LineMarkup getFormatting(long lineNumber);

}
