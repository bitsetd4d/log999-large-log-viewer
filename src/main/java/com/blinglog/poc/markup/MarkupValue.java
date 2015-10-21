package com.blinglog.poc.markup;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkupValue {

    private static Logger logger = LoggerFactory.getLogger(MarkupValue.class);

    public boolean bold;
    public Color bg;
    public Color fg;

    public MarkupValue() {
    }

    public MarkupValue(MarkupValue m1, MarkupValue m2) {
        bold = m1.bold || m2.bold;
        bg = m1.bg;
        fg = m1.fg;
        if (m2.fg != null) fg = m2.fg;
        if (m2.bg != null) bg = m2.bg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkupValue that = (MarkupValue) o;

        if (bold != that.bold) return false;
        if (bg != null ? !bg.equals(that.bg) : that.bg != null) return false;
        return !(fg != null ? !fg.equals(that.fg) : that.fg != null);

    }

    @Override
    public int hashCode() {
        int result = (bold ? 1 : 0);
        result = 31 * result + (bg != null ? bg.hashCode() : 0);
        result = 31 * result + (fg != null ? fg.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MarkupValue{" +
                "bold=" + bold +
                ", bg=" + bg +
                ", fg=" + fg +
                '}';
    }
}
