package com.log999.markup;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class MarkupValue {

    private static Logger logger = LoggerFactory.getLogger(MarkupValue.class);

    // TODO: Encapsulate back in and make immutable
    public Color bg;
    public Color fg;
    public boolean bold;

    public MarkupValue() {
    }

    public MarkupValue(Color bg, Color fg, boolean bold) {
        this.bg = bg;
        this.fg = fg;
        this.bold = bold;
    }

    public MarkupValue combinedWith(MarkupValue other) {
        Color bg = other.bg == null ? this.bg : other.bg;
        Color fg = other.fg == null ? this.fg : other.fg;
        boolean bold = this.bold || other.bold;
        return new MarkupValue(bg, fg, bold);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkupValue that = (MarkupValue) o;
        return bold == that.bold &&
                Objects.equals(bg, that.bg) &&
                Objects.equals(fg, that.fg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bg, fg, bold);
    }

    @Override
    public String toString() {
        return "MarkupValue{" +
                "bg=" + bg +
                ", fg=" + fg +
                ", bold=" + bold +
                '}';
    }
}
