package com.log999.markup;

import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LineMarkup {

    private static Logger logger = LoggerFactory.getLogger(LineMarkup.class);

    private List<Markup> markups = new ArrayList<>();

    public List<Markup> getMarkups() {
        return markups;
    }

    public void markBold(int start,int end) {
        customise(start, end, m -> m.setBold(true));
    }

    public void markBackground(int start, int end, Color bg) {
        customise(start, end, m -> m.setBackground(bg));
    }

    public void markForeground(int start, int end, Color fg) {
        customise(start, end, m -> m.setForeground(fg));
    }

    private void customise(int start,int end,Consumer<Markup> function) {
        Markup m = new Markup(start,end);
        function.accept(m);
        markups.add(m);
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        markups = normaliser.normalisedMarkups();
    }

}
