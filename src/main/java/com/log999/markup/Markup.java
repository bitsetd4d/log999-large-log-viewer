package com.log999.markup;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Markup {

    private static Logger logger = LoggerFactory.getLogger(Markup.class);

    public static final int UNBOUNDED = Integer.MAX_VALUE / 4;

    private int start;
    private int end;

    private MarkupValue value;

    public Markup(int start, int end) {
        assert end >= start;
        this.start = start;
        setEnd(end);
        this.value = new MarkupValue();
    }

    public Markup(MarkupValue value) {
        this.value = value;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = Math.min(Math.abs(end),UNBOUNDED);
    }

    public int getStart(int offset) {
        return start - offset;
    }

    public int getEnd(int offset) {
        if (end == UNBOUNDED) {
            return end;
        }
        return end - offset;
    }

    public void setBold(boolean bold) {
        value.setBold(bold);
    }

    public void setBackground(Color bg) {
        value.setBg(bg);
    }

    public void setForeground(Color fg) {
        value.setFg(fg);
    }

    // http://stackoverflow.com/questions/3269434/whats-the-most-efficient-way-to-test-two-integer-ranges-for-overlap
    public boolean affects(int offset,int length) {
        int x1 = offset;
        int x2 = offset + length;
        return x1 <= end && start <= x2;
    }

    public MarkupValue getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Markup markup = (Markup) o;
        return start == markup.start &&
                end == markup.end &&
                Objects.equals(value, markup.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, value);
    }

    @Override
    public String toString() {
        return "Markup{" +
                "start=" + start +
                ", end=" + end +
                ", value=" + value +
                '}';
    }


}
