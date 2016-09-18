package com.log999.markup;

import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;

import static com.log999.markup.matchers.MarkupMatchers.withRange;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

public class LineMarkupTest {

    private LineMarkup underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new LineMarkup();
    }

    @Test
    public void lineMarkupsEmptyInitially() {
        assertThat(underTest.getMarkups(), is(empty()));
    }

    @Test
    public void shouldCreateMarkupWithBackgroundChange() throws Exception {
        // When
        underTest.markBackground(0, 99, Color.BEIGE);
        // Then
        assertThat(underTest.getMarkups(), hasSize(1));
        assertThat(underTest.getMarkups().get(0), withRange(0, 99));
        assertThat(underTest.getMarkups().get(0).getValue().getBg(), equalTo(Color.BEIGE));
    }

    @Test
    public void shouldCreateMarkupWithForegroundChange() throws Exception {
        // When
        underTest.markForeground(5, 10, Color.AQUAMARINE);
        // Then
        assertThat(underTest.getMarkups(), hasSize(1));
        assertThat(underTest.getMarkups().get(0), withRange(5, 10));
        assertThat(underTest.getMarkups().get(0).getValue().getFg(), equalTo(Color.AQUAMARINE));
    }

    @Test
    public void shouldCreateMarkupWithBoldChange() throws Exception {
        // When
        underTest.markBold(0, 3, true);
        // Then
        assertThat(underTest.getMarkups(), hasSize(1));
        assertThat(underTest.getMarkups().get(0), withRange(0, 3));
        assertThat(underTest.getMarkups().get(0).getValue().isBold(), equalTo(true));
    }

    @Test
    public void shouldRemoveBoldChange() throws Exception {
        // Given
        underTest.markBold(0, 10, true);
        // When
        underTest.markBold(5, 6, false);
        // Then
        assertThat(underTest.getMarkups(), hasSize(2));
        assertThat(underTest.getMarkups().get(0), withRange(0, 4));
        assertThat(underTest.getMarkups().get(1), withRange(7, 10));
        assertThat(underTest.getMarkups().get(0).getValue().isBold(), equalTo(true));
        assertThat(underTest.getMarkups().get(1).getValue().isBold(), equalTo(true));
    }

    @Test
    public void shouldRemoveBackgroundChange() throws Exception {
        // Given
        underTest.markBackground(0, 10, Color.AQUAMARINE);
        // When
        underTest.markBackground(5, 6, Color.TRANSPARENT);
        // Then
        assertThat(underTest.getMarkups(), hasSize(2));
        assertThat(underTest.getMarkups().get(0), withRange(0, 4));
        assertThat(underTest.getMarkups().get(1), withRange(7, 10));
        assertThat(underTest.getMarkups().get(0).getValue().getBg(), equalTo(Color.AQUAMARINE));
        assertThat(underTest.getMarkups().get(1).getValue().getBg(), equalTo(Color.AQUAMARINE));
    }

    @Test
    public void shouldRemoveForeground() throws Exception {
        // Given
        underTest.markForeground(0, 10, Color.AQUAMARINE);
        // When
        underTest.markForeground(5, 6, Color.TRANSPARENT);
        // Then
        assertThat(underTest.getMarkups(), hasSize(2));
        assertThat(underTest.getMarkups().get(0), withRange(0, 4));
        assertThat(underTest.getMarkups().get(1), withRange(7, 10));
        assertThat(underTest.getMarkups().get(0).getValue().getFg(), equalTo(Color.AQUAMARINE));
        assertThat(underTest.getMarkups().get(1).getValue().getFg(), equalTo(Color.AQUAMARINE));
    }

    @Test
    public void removingAllFormattingShouldRemoveAllMarkups() throws Exception {
        // Given
        underTest.markForeground(0, 10, Color.AQUAMARINE);
        // When
        underTest.markForeground(0, 10, Color.TRANSPARENT);
        // Then
        assertThat(underTest.getMarkups(), hasSize(0));

    }
}
