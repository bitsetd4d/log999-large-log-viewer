package com.log999.markup.matchers;

public class MarkupMatchers {

    public static MarkupRangeMatcher withRange(int start, int end) {
        return new MarkupRangeMatcher(start, end);
    }

    public static BoldMarkupValueMatcher withBold(boolean expectation) {
        return new BoldMarkupValueMatcher(expectation);
    }

}
