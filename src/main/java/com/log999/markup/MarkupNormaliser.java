package com.log999.markup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MarkupNormaliser {

    private static Logger logger = LoggerFactory.getLogger(MarkupNormaliser.class);

    private final List<Markup> markups;
    private List<Markup> newMarkups;

    private Markup currentMarkup;
    private int x;

    public MarkupNormaliser(List<Markup> markups) {
        this.markups = markups;
    }

    public List<Markup> normalise() {
        if (markups.size() < 2) return markups;
        newMarkups = new ArrayList<>();
        int max = markups.stream().mapToInt(m -> m.getEnd(0)).max().getAsInt();
        logger.info("Normalise {} markups -> {}",markups.size(),markups);
        for (x = 0; x < max; x++) {
            MarkupValue v = getMarkupValue();
            if (v == null) {
                finishCurrentMarkupIfPresent();
            } else if (currentMarkup == null) {
                startOfNewMarkup(v);
            } else if (!currentMarkup.getValue().equals(v)) {
                finishCurrentMarkupIfPresent();
                startOfNewMarkup(v);
            }
        }
        finishCurrentMarkupIfPresent();
        Collections.sort(newMarkups, (a, b) -> Integer.compare(a.getStart(0), b.getStart(0)));
        logger.info("Transformed to {} markups -> {}", newMarkups.size(),newMarkups);
        return newMarkups;
    }

    private void finishCurrentMarkupIfPresent() {
        if (currentMarkup != null) {
            currentMarkup.setEnd(x);
            newMarkups.add(currentMarkup);
            currentMarkup = null;
        }
    }

    private void startOfNewMarkup(MarkupValue v) {
        currentMarkup = new Markup(v);
        currentMarkup.setStart(x);
    }

    private MarkupValue getMarkupValue() {
        List<MarkupValue> values = allApplicableMarkups();
        if (values.isEmpty()) return null;
        MarkupValue v = mergeMarkups(values);
        return v;
//        Markup lastMarkup = markups.get(markups.size() - 1);
//        if (x == lastMarkup.getStart(0)) return lastMarkup.getValue();
    }

    private List<MarkupValue> allApplicableMarkups() {
        return markups.stream().filter(m -> x >= m.getStart(0) && x < m.getEnd(0)).map(m -> m.getValue()).collect(Collectors.toList());
    }

    private MarkupValue mergeMarkups(List<MarkupValue> values) {
        return values.stream().reduce(null,(m1,m2) -> merge(m1,m2));
    }

    private MarkupValue merge(MarkupValue m1, MarkupValue m2) {
        if (m1 == null) return m2;
        return new MarkupValue(m1,m2);
    }

}
