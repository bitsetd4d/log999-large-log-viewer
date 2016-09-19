package com.log999.markup.matchers;

public class MarkupMatchers {

    public static MarkupRangeMatcher hasRange(int start, int end) {
        return new MarkupRangeMatcher(start, end);
    }

    public static BoldMarkupValueMatcher isBold(boolean expectation) {
        return new BoldMarkupValueMatcher(expectation);
    }

}
