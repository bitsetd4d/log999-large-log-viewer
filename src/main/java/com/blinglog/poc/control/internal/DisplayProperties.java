package com.blinglog.poc.control.internal;

import com.blinglog.poc.Globals;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class DisplayProperties {

    private static Logger logger = LoggerFactory.getLogger(DisplayProperties.class);

    private ObjectProperty<Font> fontProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Font> lineNumberFontProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Font> boldFontProperty = new SimpleObjectProperty<>();
    private FloatProperty lineHeightProperty = new SimpleFloatProperty();
    private FloatProperty charWidthProperty = new SimpleFloatProperty();

    private BooleanProperty lineWrapToWindowProperty = new SimpleBooleanProperty();
    private IntegerProperty lineWrapWidthProperty = new SimpleIntegerProperty();

    public ObjectProperty<Font> boldFontProperty() { return boldFontProperty; }
    public ObjectProperty<Font> fontProperty() { return fontProperty; }
    public ObjectProperty<Font> lineNumberFontProperty() { return lineNumberFontProperty; }
    public FloatProperty lineHeightProperty() { return lineHeightProperty; }
    public FloatProperty charWidthProperty() { return charWidthProperty; }

    public BooleanProperty lineWrapToWindowProperty() { return lineWrapToWindowProperty; }
    public IntegerProperty lineWrapWidthProperty() { return lineWrapWidthProperty; }

    private Font symbolFont;

    public DisplayProperties() {
        setFont(Font.font("monospace", 12), Font.font("monospace", FontWeight.BOLD, 12));
        lineWrapWidthProperty.setValue(Globals.HARD_LINEWRAP);
    }

    private void setFont(Font font,Font boldFont) {
        System.out.println("Setting font to "+font);
        Toolkit toolkit = Toolkit.getToolkit();
        FontLoader fontLoader = toolkit.getFontLoader();
        FontMetrics fontMetrics = fontLoader.getFontMetrics(font);
        lineHeightProperty.setValue(fontMetrics.getLineHeight());
        charWidthProperty.setValue(toolkit.getFontLoader().computeStringWidth("M",font));
        fontProperty.setValue(font); // Update last
        boldFontProperty.setValue(boldFont);
        lineNumberFontProperty.setValue(changeSize(font,-2));
        loadSymbolFont();
    }

    private void loadSymbolFont() {
        try {
            InputStream resourceAsStream = DisplayProperties.class.getResourceAsStream("/editorstyle/fontawesome/fontawesome-webfont.ttf");
            symbolFont = Font.loadFont(resourceAsStream, 10);
        } catch (Exception e) {
            logger.error("Error loading symbol font",e);
        }
    }

    public void increaseFontSize() {
        Font current = fontProperty.get();
        Font currentBold = boldFontProperty.get();
        setFont(changeSize(current,1),changeSize(currentBold,1));
    }

    public void decreaseFontSize() {
        Font current = fontProperty.get();
        Font currentBold = boldFontProperty.get();
        setFont(changeSize(current,-1),changeSize(currentBold,-1));
    }

    private Font changeSize(Font f,int delta) {
        return new Font(f.getName(),f.getSize() + delta);
    }

    public Font getSymbolFont() {
        return symbolFont;
    }
}
