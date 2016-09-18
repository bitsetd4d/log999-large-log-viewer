package com.log999.markup;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class MarkupValue {

    private static Logger logger = LoggerFactory.getLogger(MarkupValue.class);

    // TODO: Make immutable
    private Color bg;
    private Color fg;
    private Boolean bold;

    public MarkupValue() {
    }

    public MarkupValue(Color bg, Color fg) {
        this.bg = bg;
        this.fg = fg;
    }

    public MarkupValue combinedWith(MarkupValue other) {
        Color bg = other.getBg() == null ? this.getBg() : other.getBg();
        Color fg = other.getFg() == null ? this.getFg() : other.getFg();
        MarkupValue newMarkupValue = new MarkupValue(bg, fg);
        newMarkupValue.bold = other.bold == null ? this.bold : other.bold;;
        return newMarkupValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkupValue that = (MarkupValue) o;
        return isBold() == that.isBold() &&
                Objects.equals(getBg(), that.getBg()) &&
                Objects.equals(getFg(), that.getFg());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBg(), getFg(), isBold());
    }

    @Override
    public String toString() {
        return "MarkupValue{" +
                "bg=" + getBg() +
                ", fg=" + getFg() +
                ", bold=" + isBold() +
                '}';
    }

    public Color getBg() {
        return bg;
    }

    public void setBg(Color bg) {
        requireNonNull(bg);
        this.bg = bg;
    }

    public Color getFg() {
        return fg;
    }

    public void setFg(Color fg) {
        requireNonNull(fg);
        this.fg = fg;
    }

    public boolean isBold() {
        return bold == Boolean.TRUE;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isBlank() {
        return isBlank(fg) && isBlank(bg) && bold != Boolean.TRUE;
    }

    private boolean isBlank(Color color) {
        return color == null || color.equals(Color.TRANSPARENT);
    }
}
