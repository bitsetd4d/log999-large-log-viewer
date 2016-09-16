package com.log999.markup;

import javafx.scene.paint.Color;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;


public class MarkupNormaliserTest {

    @Test
    public void shouldWorkWithEmptyList() throws Exception {
        MarkupNormaliser normaliser = new MarkupNormaliser(Collections.emptyList());
        List<Markup> normalised = normaliser.normalisedMarkups();
        assertThat(normalised, is(empty()));
    }

    @Test
    public void shouldCombined2AdjacentCells() throws Exception {
        // Given
        Markup a = new Markup(1,1);
        Markup b = new Markup(2,2);
        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(1));
        Markup markup = normalisedMarkups.get(0);
        assertThat(markup.getStart(0), equalTo(1));
        assertThat(markup.getEnd(0), equalTo(2));
    }

    @Test
    public void shouldLeave2UnadjacentCellsAlone() throws Exception {
        // Given
        Markup a = new Markup(1,1);
        Markup b = new Markup(4,5);
        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(2));
        assertThat(a, equalTo(normalisedMarkups.get(0)));
        assertThat(b, equalTo(normalisedMarkups.get(1)));
    }

    @Test
    public void shouldReturnUnorderedMarkupsInOrder() throws Exception {
        // Given
        Markup a = new Markup(10,11);
        Markup b = new Markup(20,21);
        Markup c = new Markup(30,31);
        List<Markup> markups = Arrays.asList(c, a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(3));
        assertThat(normalisedMarkups.get(0), equalTo(a));
        assertThat(normalisedMarkups.get(1), equalTo(b));
        assertThat(normalisedMarkups.get(2), equalTo(c));
    }

    @Test
    public void shouldCombineAdjacentRangesWithSameMarkup() throws Exception {
        // Given
        Markup a = new Markup(1,1);
        Markup b = new Markup(2,2);
        a.setBold(true); a.setBackground(Color.AQUA);
        b.setBold(true); b.setBackground(Color.AQUA);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(1));
        Markup markup = normalisedMarkups.get(0);
        assertThat(markup.getStart(0), equalTo(1));
        assertThat(markup.getEnd(0), equalTo(2));
    }

    @Test
    public void shouldNotCombineAdjacentRangesWithDifferentMarkup() throws Exception {
        // Given
        Markup a = new Markup(1,1);
        Markup b = new Markup(2,2);
        a.setBold(false); a.setBackground(Color.AQUA);
        b.setBold(true); b.setBackground(Color.AQUA);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(2));
        assertThat(normalisedMarkups.get(0), equalTo(a));
        assertThat(normalisedMarkups.get(1), equalTo(b));
    }

    @Test
    public void shouldMergeMarkupsForSameRanges() throws Exception {
        // Given
        Markup a = new Markup(2,10);
        Markup b = new Markup(2,10);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(1));
        assertThat(normalisedMarkups.get(0), equalTo(a));
    }

    @Test
    public void shouldCreate3MarkupsFor2OverlappingRangesRangeIsPartialSubsetOfOther() throws Exception {
        // Given
        Markup a = new Markup(1,10);
        Markup b = new Markup(5,15);

        a.setBold(false); a.setBackground(Color.AQUA);
        b.setBold(true); b.setForeground(Color.BLUE);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(3));
        assertThat(normalisedMarkups.get(0), withRange(1,4));
        assertThat(normalisedMarkups.get(1), withRange(5,10));
        assertThat(normalisedMarkups.get(2), withRange(11,15));
    }

    @Test
    public void shouldCreate3MarkupsFor2OverlappingRangesWhereRangeIsFullSubsetOfOther() throws Exception {
        // Given
        Markup a = new Markup(1,20);
        Markup b = new Markup(10,15);

        a.setBold(false); a.setBackground(Color.AQUA);
        b.setBold(true); b.setForeground(Color.BLUE);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups.size(), equalTo(3));
        assertThat(normalisedMarkups.get(0), withRange(1,9));
        assertThat(normalisedMarkups.get(1), withRange(10,15));
        assertThat(normalisedMarkups.get(2), withRange(16,20));
    }

    private static MarkupRangeMatcher withRange(int start, int end) {
        return new MarkupRangeMatcher(start, end);
    }

    private static class MarkupRangeMatcher extends BaseMatcher<Markup> {

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
}
