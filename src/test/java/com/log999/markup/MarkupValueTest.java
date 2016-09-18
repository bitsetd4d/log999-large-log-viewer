package com.log999.markup;

import javafx.scene.paint.Color;
import org.junit.Test;

import static com.log999.markup.matchers.MarkupMatchers.withBold;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MarkupValueTest {

    private MarkupValue createMarkupValue(Color bg, Color fg, boolean bold) {
        MarkupValue markupValue = new MarkupValue(bg, fg);
        markupValue.setBold(bold);
        return markupValue;
    }

    @Test
    public void checkMarkValuesEqual() throws Exception {
        MarkupValue value = createMarkupValue(Color.BLACK, Color.WHITE, false);
        MarkupValue valueTheSame = createMarkupValue(Color.BLACK, Color.WHITE, false);
        assertThat(value, is(equalTo(valueTheSame)));
    }

    @Test
    public void checkValuesNotEqual() throws Exception {
        MarkupValue baseValue = createMarkupValue(Color.BLACK, Color.WHITE, false);
        assertThat(baseValue, is(not(equalTo(createMarkupValue(Color.BLUE, Color.WHITE, false)))));
        assertThat(baseValue, is(not(equalTo(createMarkupValue(Color.BLACK, Color.GREEN, false)))));
        assertThat(baseValue, is(not(equalTo(createMarkupValue(Color.BLACK, Color.WHITE, true)))));
    }

    @Test
    public void checkForegroundWithNullEqual() throws Exception {
        MarkupValue v1a = createMarkupValue(null, Color.WHITE, false);
        MarkupValue v1b = createMarkupValue(null, Color.WHITE, false);
        assertThat(v1a, is(equalTo(v1b)));
    }

    @Test
    public void checkBackgroundWithNullEqual() throws Exception {
        MarkupValue v1a = createMarkupValue(Color.AQUAMARINE, null, false);
        MarkupValue v1b = createMarkupValue(Color.AQUAMARINE, null, false);
        assertThat(v1a, is(equalTo(v1b)));
    }

    @Test
    public void checkBackgroundAndForegroundWithNullEqual() throws Exception {
        MarkupValue v1a = createMarkupValue(null, null, false);
        MarkupValue v1b = createMarkupValue(null, null, false);
        assertThat(v1a, is(equalTo(v1b)));
    }

    @Test
    public void checkEqualObjectHashesSame() {
        MarkupValue a = createMarkupValue(null, Color.AQUA, false);
        MarkupValue b = createMarkupValue(null, Color.AQUA, false);
        assertThat(a.hashCode(), is(equalTo(b.hashCode())));
    }

    @Test
    public void checkUnequalObjectProduceDifferentHashes() {
        MarkupValue baseValue = createMarkupValue(Color.BLACK, Color.WHITE, false);
        assertThat(baseValue.hashCode(), is(not(equalTo(createMarkupValue(Color.BLUE, Color.WHITE, false).hashCode()))));
        assertThat(baseValue.hashCode(), is(not(equalTo(createMarkupValue(Color.BLACK, Color.GREEN, false).hashCode()))));
        assertThat(baseValue.hashCode(), is(not(equalTo(createMarkupValue(Color.BLACK, Color.WHITE, true).hashCode()))));
    }

    @Test
    public void valuesCombine_Bold() throws Exception {
        MarkupValue a = createMarkupValue(null, null, false);
        MarkupValue b = createMarkupValue(null, null, true);

        assertThat(a.combinedWith(b), withBold(true));
        assertThat(b.combinedWith(a), withBold(false));
    }

    @Test
    public void valuesCombine_Foreground() throws Exception {
        MarkupValue a = createMarkupValue(Color.AQUA, null, false);
        MarkupValue b = createMarkupValue(Color.RED, null, true);
        MarkupValue expected = createMarkupValue(Color.RED, null, true);

        assertThat(a.combinedWith(b), is(equalTo(expected)));
    }

    @Test
    public void valuesCombine_Background() throws Exception {
        MarkupValue a = createMarkupValue(null, Color.AQUA, false);
        MarkupValue b = createMarkupValue(null, Color.RED, true);
        MarkupValue expected = createMarkupValue(null, Color.RED, true);

        assertThat(a.combinedWith(b), is(equalTo(expected)));
    }

    @Test
    public void isBlankMarkupIfNoValuesSet() {
        MarkupValue markupValue = new MarkupValue();
        assertThat(markupValue.isBlank(), is(equalTo(true)));
    }

    @Test
    public void isBlankMarkupWhenValuesBoldReset() {
        // Given
        MarkupValue markupValue = new MarkupValue();
        markupValue.setBold(true);
        assertThat(markupValue.isBlank(), is(equalTo(false)));

        // When
        markupValue.setBold(false);
        assertThat(markupValue.isBlank(), is(equalTo(true)));
    }

    @Test
    public void isBlankMarkupWhenForegroundReset() {
        // Given
        MarkupValue markupValue = new MarkupValue();
        markupValue.setFg(Color.BISQUE);
        assertThat(markupValue.isBlank(), is(equalTo(false)));

        // When
        markupValue.setFg(Color.TRANSPARENT);
        assertThat(markupValue.isBlank(), is(equalTo(true)));
    }

    @Test
    public void isBlankMarkupWhenBackgroundReset() {
        // Given
        MarkupValue markupValue = new MarkupValue();
        markupValue.setBg(Color.BISQUE);
        assertThat(markupValue.isBlank(), is(equalTo(false)));

        // When
        markupValue.setBg(Color.TRANSPARENT);
        assertThat(markupValue.isBlank(), is(equalTo(true)));
    }

    @Test
    public void isBlankMarkupWhenAllReset() {
        // Given
        MarkupValue markupValue = new MarkupValue();
        markupValue.setFg(Color.BISQUE);
        markupValue.setBg(Color.BISQUE);
        markupValue.setBold(true);
        assertThat(markupValue.isBlank(), is(equalTo(false)));

        // When
        markupValue.setFg(Color.TRANSPARENT);
        markupValue.setBg(Color.TRANSPARENT);
        markupValue.setBold(false);
        assertThat(markupValue.isBlank(), is(equalTo(true)));
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullIsNotAllowedForForeground() {
        MarkupValue markupValue = new MarkupValue();
        markupValue.setFg(null);
    }

    @Test(expected = NullPointerException.class)
    public void ensureNullIsNotAllowedForBackground() {
        MarkupValue markupValue = new MarkupValue();
        markupValue.setBg(null);
    }
}
