package com.log999.markup;

import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.log999.markup.matchers.MarkupMatchers.hasRange;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;


public class MarkupNormaliserTest {

    @Test
    public void shouldWorkWithEmptyList() throws Exception {
        MarkupNormaliser normaliser = new MarkupNormaliser(Collections.emptyList());
        List<Markup> normalised = normaliser.normalisedMarkups();
        assertThat(normalised, is(empty()));
    }

    private Markup newMarkup(int start, int end) {
        Markup m = new Markup(start, end);
        m.setForeground(Color.AQUA);
        return m;
    }

    @Test
    public void shouldCombined2AdjacentCells() throws Exception {
        // Given
        Markup a = newMarkup(1, 1);
        Markup b = newMarkup(2, 2);
        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(1));
        Markup markup = normalisedMarkups.get(0);
        assertThat(markup.getStart(0), equalTo(1));
        assertThat(markup.getEnd(0), equalTo(2));
    }

    @Test
    public void shouldLeave2UnadjacentCellsAlone() throws Exception {
        // Given
        Markup a = newMarkup(1, 1);
        Markup b = newMarkup(4, 5);
        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(2));
        assertThat(a, equalTo(normalisedMarkups.get(0)));
        assertThat(b, equalTo(normalisedMarkups.get(1)));
    }

    @Test
    public void shouldReturnUnorderedMarkupsInOrder() throws Exception {
        // Given
        Markup a = newMarkup(10, 11);
        Markup b = newMarkup(20, 21);
        Markup c = newMarkup(30, 31);
        List<Markup> markups = Arrays.asList(c, a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(3));
        assertThat(normalisedMarkups.get(0), equalTo(a));
        assertThat(normalisedMarkups.get(1), equalTo(b));
        assertThat(normalisedMarkups.get(2), equalTo(c));
    }

    @Test
    public void shouldCombineAdjacentRangesWithSameMarkup() throws Exception {
        // Given
        Markup a = newMarkup(1, 1);
        Markup b = newMarkup(2, 2);
        a.setBold(true);
        a.setBackground(Color.AQUA);
        b.setBold(true);
        b.setBackground(Color.AQUA);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(1));
        Markup markup = normalisedMarkups.get(0);
        assertThat(markup.getStart(0), equalTo(1));
        assertThat(markup.getEnd(0), equalTo(2));
    }

    @Test
    public void shouldNotCombineAdjacentRangesWithDifferentMarkup() throws Exception {
        // Given
        Markup a = newMarkup(1, 1);
        Markup b = newMarkup(2, 2);
        a.setBold(false);
        a.setBackground(Color.AQUA);
        b.setBold(true);
        b.setBackground(Color.AQUA);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(2));
        assertThat(normalisedMarkups.get(0), equalTo(a));
        assertThat(normalisedMarkups.get(1), equalTo(b));
    }

    @Test
    public void shouldMergeMarkupsForSameRanges() throws Exception {
        // Given
        Markup a = newMarkup(2, 10);
        Markup b = newMarkup(2, 10);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(1));
        assertThat(normalisedMarkups.get(0), equalTo(a));
    }

    @Test
    public void shouldCreate3MarkupsFor2OverlappingRangesRangeIsPartialSubsetOfOther() throws Exception {
        // Given
        Markup a = newMarkup(1, 10);
        Markup b = newMarkup(5, 15);

        a.setBold(false);
        a.setBackground(Color.AQUA);
        b.setBold(true);
        b.setForeground(Color.BLUE);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(3));
        assertThat(normalisedMarkups.get(0), hasRange(1, 4));
        assertThat(normalisedMarkups.get(1), hasRange(5, 10));
        assertThat(normalisedMarkups.get(2), hasRange(11, 15));
    }

    @Test
    public void shouldCreate3MarkupsFor2OverlappingRangesWhereRangeIsFullSubsetOfOther() throws Exception {
        // Given
        Markup a = newMarkup(1, 20);
        Markup b = newMarkup(10, 15);

        a.setBold(false);
        a.setBackground(Color.AQUA);
        b.setBold(true);
        b.setForeground(Color.BLUE);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(3));
        assertThat(normalisedMarkups.get(0), hasRange(1, 9));
        assertThat(normalisedMarkups.get(1), hasRange(10, 15));
        assertThat(normalisedMarkups.get(2), hasRange(16, 20));
    }

    @Test
    public void resettingMarkupsShouldRemoveFromListIfTheyAreBlank() throws Exception {
        // Given
        Markup a = newMarkup(1, 20);
        Markup b = newMarkup(10, 15);

        b.setBold(false);
        b.setForeground(Color.TRANSPARENT);
        b.setBackground(Color.TRANSPARENT);

        List<Markup> markups = Arrays.asList(a, b);

        // When
        MarkupNormaliser normaliser = new MarkupNormaliser(markups);
        List<Markup> normalisedMarkups = normaliser.normalisedMarkups();

        // Then
        assertThat(normalisedMarkups, hasSize(2));
        assertThat(normalisedMarkups.get(0), hasRange(1, 9));
        assertThat(normalisedMarkups.get(1), hasRange(16, 20));
    }

}
