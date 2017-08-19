package com.blinglog.poc.file.internal;

import com.log999.display.api.LogFileLine;
import com.log999.display.internal.LogFileLineImpl;
import com.log999.markup.Markup;
import com.log999.markup.MarkupMemory;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

@RunWith(MockitoJUnitRunner.class)
public class LogFileDisplayRowImpl_DrawSections {

    @Mock
    private MarkupMemory markupMemory;

    private LogFileDisplayRowImpl underTest;
    private final String logLine = "01234567890ABCD";

    @Before
    public void setUp() throws Exception {
        LogFileLine logFileLine = new LogFileLineImpl(0, markupMemory, logLine);
        underTest = (LogFileDisplayRowImpl) logFileLine.getDisplayRows()[0];
    }

    private void pending() {
        assumeThat("TODO", true, is(equalTo(false)));
    }

    @Test
    public void testNoMarkupsProducesSingleDrawSection() throws Exception {
        System.out.println("--------------");
        List<DrawSection> drawSections = underTest.getDrawSections(Collections.emptyList());

        assertThat(drawSections, hasSize(1));
        DrawSection ds = drawSections.get(0);
        assertThat(ds.getLength(), equalTo(logLine.length()));
        assertThat(ds.getMarkup(), is(nullValue()));
    }

    @Test
    public void markupsForAllSectionsProduced_BeginningOnly() throws Exception {
        System.out.println("XXX-----------");
        Markup markup = newMarkup(0, 2);
        final List<Markup> drawSectionMarkups = getDrawSectionMarkups(markup);

        assertThat(drawSectionMarkups, hasSize(2));
        assertThat(drawSectionMarkups.get(0), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(1), is(nullValue()));
    }

    @Test
    public void markupsForAllSectionsProduced_EndOnly() throws Exception {
        System.out.println("-----------XXX");
        Markup markup = newMarkup(10, 15);
        final List<Markup> drawSectionMarkups = getDrawSectionMarkups(markup);

        assertThat(drawSectionMarkups, hasSize(2));
        assertThat(drawSectionMarkups.get(0), is(nullValue()));
        assertThat(drawSectionMarkups.get(1), is(not(nullValue())));
    }

    @Test
    public void markupsForAllSectionsProduced_MiddleOnly() throws Exception {
        System.out.println("------XXX-----");
        Markup markup = newMarkup(5, 10);
        final List<Markup> drawSectionMarkups = getDrawSectionMarkups(markup);

        assertThat(drawSectionMarkups, hasSize(3));
        assertThat(drawSectionMarkups.get(0), is(nullValue()));
        assertThat(drawSectionMarkups.get(1), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(2), is(nullValue()));
    }

    @Test
    public void markupsForAllSectionsProduced_MiddleAndMiddle() throws Exception {
        System.out.println("---XXX---XXX--");
        Markup markup1 = newMarkup(3, 7);
        Markup markup2 = newMarkup(10, 13);
        final List<Markup> drawSectionMarkups = getDrawSectionMarkups(markup1, markup2);

        assertThat(drawSectionMarkups, hasSize(5));
        assertThat(drawSectionMarkups.get(0), is(nullValue()));
        assertThat(drawSectionMarkups.get(1), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(2), is(nullValue()));
        assertThat(drawSectionMarkups.get(3), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(4), is(nullValue()));
    }

    @Test
    public void markupsForAllSectionsProduced_BeginningAndEnd() throws Exception {
        System.out.println("XXX--------XXX");
        Markup markup1 = newMarkup(0, 3);
        Markup markup2 = newMarkup(12, 15);
        final List<Markup> drawSectionMarkups = getDrawSectionMarkups(markup1, markup2);

        assertThat(drawSectionMarkups, hasSize(3));
        assertThat(drawSectionMarkups.get(0), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(1), is(nullValue()));
        assertThat(drawSectionMarkups.get(2), is(not(nullValue())));
    }

    @Test
    public void markupsForAllSectionsProduced_BeginningAndMiddle() throws Exception {
        System.out.println("XXX---XXX-----");
        Markup markup1 = newMarkup(0, 3);
        Markup markup2 = newMarkup(8, 11);
        final List<Markup> drawSectionMarkups = getDrawSectionMarkups(markup1, markup2);

        assertThat(drawSectionMarkups, hasSize(4));
        assertThat(drawSectionMarkups.get(0), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(1), is(nullValue()));
        assertThat(drawSectionMarkups.get(2), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(3), is(nullValue()));
    }

    @Test
    public void markupsForAllSectionsProduced_MiddleAndEnd() throws Exception {
        System.out.println("------XXX--XXX");
        Markup markup1 = newMarkup(5, 9);
        Markup markup2 = newMarkup(12, 15);
        final List<Markup> drawSectionMarkups = getDrawSectionMarkups(markup1, markup2);

        assertThat(drawSectionMarkups, hasSize(4));
        assertThat(drawSectionMarkups.get(0), is(nullValue()));
        assertThat(drawSectionMarkups.get(1), is(not(nullValue())));
        assertThat(drawSectionMarkups.get(2), is(nullValue()));
        assertThat(drawSectionMarkups.get(3), is(not(nullValue())));
    }

    private Markup newMarkup(int start, int end) {
        Markup m = new Markup(start, end);
        m.setForeground(Color.AQUA);
        return m;
    }

    private List<Markup> getDrawSectionMarkups(Markup... markups) {
        final List<Markup> markupList = Arrays.asList(markups);
        List<DrawSection> drawSections = underTest.getDrawSections(markupList);
        return drawSections.stream()
                .map(DrawSection::getMarkup)
                .collect(Collectors.toList());
    }

}
