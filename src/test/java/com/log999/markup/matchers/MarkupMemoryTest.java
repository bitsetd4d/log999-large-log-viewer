package com.log999.markup.matchers;

import com.log999.markup.LineMarkup;
import com.log999.markup.Markup;
import com.log999.markup.MarkupMemoryImpl;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;

import static com.log999.markup.matchers.MarkupMatchers.hasRange;
import static com.log999.markup.matchers.MarkupMatchers.isBold;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;

public class MarkupMemoryTest {

    private MarkupMemoryImpl underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new MarkupMemoryImpl();
    }

    @Test
    public void lineMarkupShouldWorkWhenNoExistingMarkupOnTheLine() {
        LineMarkup formatting = underTest.getFormatting(22);
        assertThat(formatting, notNullValue());
    }

    @Test
    public void checkSetBoldMarkupOnLine() {
        // When
        underTest.setBold(99, 10, 20, true);
        LineMarkup formatting = underTest.getFormatting(99);
        // Then
        assertThat(formatting.getMarkups(), hasSize(1));
        Markup markup = formatting.getMarkups().get(0);
        assertThat(markup, hasRange(10, 20));
        assertThat(markup.getValue(), isBold(true));
    }

    @Test
    public void checkClearBoldMarkupOnLine() {
        // Given
        underTest.setBold(99, 10, 20, true);
        // When
        underTest.setBold(99, 10, 20, false);
        LineMarkup formatting = underTest.getFormatting(99);
        // Then
        assertThat(formatting.getMarkups(), hasSize(0));
    }

    @Test
    public void checkSettingForegroundOnLine() {
        // When
        underTest.setForeground(99, 10, 20, Color.ALICEBLUE);
        LineMarkup formatting = underTest.getFormatting(99);
        // Then
        assertThat(formatting.getMarkups(), hasSize(1));
        Markup markup = formatting.getMarkups().get(0);
        assertThat(markup, hasRange(10, 20));
        assertThat(markup.getValue().getFg(), equalTo(Color.ALICEBLUE));
    }

    @Test
    public void clearingFormattingOnLineShouldRemoveLineFormattingFromMemory() {
        // Given
        underTest.setForeground(1000, 10, 20, Color.RED);
        assertThat(underTest.testOnlyCountLineMarkups(), equalTo(1));
        // When
        underTest.setForeground(1000, 10, 20, Color.TRANSPARENT);
        // Then
        assertThat(underTest.testOnlyCountLineMarkups(), equalTo(0));
    }

    @Test
    public void settingFormattingOnLineMarkupShouldBeSavedInMemory() {
        // Given
        underTest.setForeground(999, 10, 20, Color.RED);
        // When
        LineMarkup formatting = underTest.getFormatting(999);
        // Then
        assertThat(formatting.getMarkups(), hasSize(1));
        assertThat(formatting.getMarkups().get(0), hasRange(10, 20));
        assertThat(formatting.getMarkups().get(0).getValue().getFg(), equalTo(Color.RED));
    }


    @Test
    public void checkMarkingRangeOfLines() throws Exception {
        // When
        underTest.setMarked(10, 12, true);
        // Then
        assertThat(underTest.isMarked(9), equalTo(false));
        assertThat(underTest.isMarked(10), equalTo(true));
        assertThat(underTest.isMarked(11), equalTo(true));
        assertThat(underTest.isMarked(12), equalTo(true));
        assertThat(underTest.isMarked(13), equalTo(false));
    }

    @Test
    public void checkUmmarkingRangeOfLines() throws Exception {
        // Given
        underTest.setMarked(10, 13, true);
        // When
        underTest.setMarked(11, 12, false);
        // Then
        assertThat(underTest.isMarked(9), equalTo(false));
        assertThat(underTest.isMarked(10), equalTo(true));
        assertThat(underTest.isMarked(11), equalTo(false));
        assertThat(underTest.isMarked(12), equalTo(false));
        assertThat(underTest.isMarked(13), equalTo(true));
        assertThat(underTest.isMarked(14), equalTo(false));
    }
}
