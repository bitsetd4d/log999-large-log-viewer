package com.log999.markup.matchers;

import com.log999.markup.Markup;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class MarkupRangeMatcher extends BaseMatcher<Markup> {

    private final int start;
    private final int end;

    public MarkupRangeMatcher(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean matches(Object item) {
        return item instanceof Markup && ((Markup) item).getStart(0) == start && ((Markup) item).getEnd(0) == end;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a markup with range ");
        description.appendValue(start);
        description.appendText(" to ");
        description.appendValue(end);
    }
}
