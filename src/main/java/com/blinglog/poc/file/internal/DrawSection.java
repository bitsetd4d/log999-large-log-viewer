package com.blinglog.poc.file.internal;

import com.blinglog.poc.control.internal.DisplayProperties;
import com.log999.markup.Markup;
import com.log999.markup.MarkupValue;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawSection {

    private static Logger logger = LoggerFactory.getLogger(DrawSection.class);

    private final Markup markup;
    private final String textBlock;

    public DrawSection(Markup markup, String textBlock) {
        this.markup = markup;
        this.textBlock = textBlock;
    }

    public int getLength() {
        return textBlock.length();
    }

    void draw(GraphicsContext gc, double x, double y, DisplayProperties dp) {
        gc.save();
        if (markup != null) {
            MarkupValue value = markup.getValue();
            Color bg = value.getBg();
            if (bg != null) {
                gc.save();
                gc.setFill(bg);
                double w = textBlock.length() * dp.charWidthProperty().doubleValue();
                double h = dp.lineHeightProperty().doubleValue();
                double top = y - h+2;
                gc.fillRect(x,top,w,h);
                gc.restore();
            }
            Color fg = value.getFg();
            if (fg != null) {
                gc.setFill(fg);
            }
            if (value.isBold()) {
                gc.setFont(dp.boldFontProperty().get());
            } else {
                gc.setFont(dp.fontProperty().get());
            }
        }
        gc.fillText(textBlock,x,y);
        gc.restore();
    }

    @Override
    public String toString() {
        return "DrawSection{" +
                "markup=" + markup +
                ", textBlock='" + trim(textBlock) + '\'' +
                '}';
    }

    private String trim(String x) {
        if (x == null) return "null";
        if (x.length() > 60) {
            return x.substring(0,57) + "...";
        }
        return x;
    }
}
