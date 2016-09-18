package com.log999.markup.matchers;

import com.log999.markup.MarkupValue;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class BoldMarkupValueMatcher extends BaseMatcher<MarkupValue> {

    private final boolean expectation;

    public BoldMarkupValueMatcher(boolean expectation) {
        this.expectation = expectation;
    }

    @Override
    public boolean matches(Object item) {
        return item instanceof MarkupValue && ((MarkupValue) item).isBold() == expectation;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a markup value with bold ");
        description.appendValue(expectation);
    }
}