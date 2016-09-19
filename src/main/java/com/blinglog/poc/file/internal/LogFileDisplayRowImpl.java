package com.blinglog.poc.file.internal;

import com.blinglog.poc.control.internal.DisplayProperties;
import com.blinglog.poc.file.LogFileDisplayRow;
import com.log999.markup.LineMarkup;
import com.log999.markup.Markup;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogFileDisplayRowImpl implements LogFileDisplayRow {

    private static Logger logger = LoggerFactory.getLogger(LogFileDisplayRowImpl.class);

    private final LogFileLineImpl logFileLine;
    private final String text;
    private final int offset;

    private final int displayRowIndex;

    private static final boolean DEBUG_DRAW = false;
    private static final long DEBUG_ROW_START = 14;
    private static final long DEBUG_ROW_END = 14;

    private boolean shouldDebug() {
        return DEBUG_DRAW && logFileLine.getLineNumber() >= DEBUG_ROW_START && logFileLine.getLineNumber() <= DEBUG_ROW_END;
    }

    public LogFileDisplayRowImpl(LogFileLineImpl logFileLine,int offset,String text,int displayRowIndex) {
        this.logFileLine = logFileLine;
        this.offset = offset;
        this.text = text;
        this.displayRowIndex = displayRowIndex;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void markBold(int startx, int endx, boolean bold) {
        logger.info("Line {}/{} - MARK BOLD {} - {}",logFileLine.getLineNumber(), displayRowIndex,startx,endx);
        if (logFileLine != null) {
            logFileLine.markBold(startx + offset,endx + offset, bold);
        }
    }

    @Override
    public void markSelectedBackground(int start, int end, Color bg) {
        logger.info("Line {}/{} - MARK BACKGROUND {} - {}",logFileLine.getLineNumber(), displayRowIndex,start,end);
        if (logFileLine != null) {
            logFileLine.markBackground(start + offset, end + offset, bg);
        }
    }

    @Override
    public void markSelectedForeground(int start, int end, Color bg) {
        logger.info("Line {}/{} - MARK FOREGROUND {} - {}",logFileLine.getLineNumber(), displayRowIndex,start,end);
        if (logFileLine != null) {
            logFileLine.markForeground(start + offset, end + offset, bg);
        }
    }

    @Override
    public void drawText(GraphicsContext gc, double x, double y, DisplayProperties displayProperties) {
        double charWidth = displayProperties.charWidthProperty().get();
        if (logFileLine == null) return;
        if (shouldDebug()) {
            logger.info("==START DRAW== Line {} / Display Row {} ===========================================================", logFileLine.getLineNumber(), displayRowIndex);
        }
        LineMarkup markup = logFileLine.getLineMarkup();
        List<Markup> markups = markup.getMarkups();
        if (shouldDebug() && displayRowIndex == 0) {
            if (markups.size() > 0) {
                logger.info("********** These are all Markups for log line {}",logFileLine.getLineNumber());
                for (Markup m : markups) {
                    logger.info(" * {}",m);
                }
                logger.info("**********");
            }
        }
        List<DrawSection> sections = getDrawSections(markups);
        double startx = x;
        if (shouldDebug()) {
            logger.info("-- {}/{} Draw Sections --", logFileLine.getLineNumber(), displayRowIndex);
        }
        for (DrawSection s : sections) {
            if (shouldDebug()) {
                logger.info("    Section: {}",s);
            }
            s.draw(gc,startx,y,displayProperties);
            startx += s.getLength() * charWidth;
        }
        if (shouldDebug()) {
            logger.info("==END DRAW== Line {} / Display Row {} ===========================================================", logFileLine.getLineNumber(), displayRowIndex);
        }
    }

    private List<DrawSection> getDrawSections(List<Markup> markups) {
        String text = getText();
        List<DrawSection> sections = new ArrayList<>();
        if (markups.isEmpty()) {
            sections.add(new DrawSection(null,text));
            return sections;
        }
        List<Markup> applicableMarkups = getMarkupsThatAffectThisDisplayRow(markups);
        if (shouldDebug()) {
            logger.info("@@@@ Row is offset {} These markups apply -> {}",offset,applicableMarkups);
        }
        Markup first = markups.get(0);
        /*
         * If there is unformatted text before first markup add a
         * DrawSection for this
         */
        int gotUpTo = 0;
        if (first.getStart(offset) > 0) {
            if (shouldDebug())
                logger.info("~~~   (Filled gap before first markup {})",first);
            int cut = Math.min(first.getStart(offset),text.length());
            DrawSection s = new DrawSection(null,text.substring(0,cut));
            sections.add(s);
            gotUpTo = first.getStart(offset);
        }

        /*
         * Loop through markups. Add formatted/unformatted DrawSections
         */
        for (Markup m : markups) {
            if (shouldDebug()) logger.info("~~~   (Up to position {})",gotUpTo);
            boolean affected = m.affects(offset,text.length());
            int startOfMarkup = m.getStart(offset);
            int startOfText = Math.max(0, startOfMarkup);
            if (affected) {
                if (startOfMarkup > gotUpTo) {
                    // Fill in gaps between markups
                    String textBlock = text.substring(gotUpTo, startOfMarkup);
                    sections.add(new DrawSection(null, textBlock));
                    gotUpTo = startOfText;
                }
                int end = m.getEnd(offset);
                String textBlock;
                if (end > text.length()) {
                    if (shouldDebug()) logger.info("~~~   (Markup covers whole line)");
                    textBlock = text.substring(startOfText);
                } else {
                    if (shouldDebug()) logger.info("~~~   (Markup ends before end of line)");
                    textBlock = text.substring(gotUpTo, end);
                }
                sections.add(new DrawSection(m, textBlock));
                gotUpTo = end;
            } else {
                if (shouldDebug()) logger.info("~~~   (Markup not for me {} - {})",startOfText,markups);
            }
        }
        Markup last = markups.get(markups.size() - 1);
        int finalEnd = last.getEnd(offset);
        if (finalEnd >= 0 && finalEnd < text.length()) {
            String textBlock = text.substring(finalEnd);
            sections.add(new DrawSection(null, textBlock));
            if (shouldDebug()) logger.info("~~~   (Add final empty drawsection from {})",finalEnd);
        }
        if (sections.isEmpty()) {
            if (shouldDebug()) {
                logger.info("~~~   (No Markup applied - default added)");
            }
            sections.add(new DrawSection(null, text));
        }
        return sections;
    }

    private List<Markup> getMarkupsThatAffectThisDisplayRow(List<Markup> markups) {
        return markups
                .stream()
                .filter(m -> m.affects(offset,text.length()))
                .collect(Collectors.toList());
    }

    private String truncate(String x) {
        if (x.length() < 100) return x;
        return x.substring(0,100);
    }

    @Override
    public void dumpToLog(int i) {
        logger.info("LINE[{}] Line {} (Display {} offset {}) - {}",i,logFileLine.getLineNumber(), displayRowIndex,offset,truncate(text));
    }

    @Override
    public String toString() {
        return "LogFileDisplayRowImpl{" +
                "logFileLine=" + logFileLine +
                ", text='" + text + '\'' +
                ", offset=" + offset +
                ", displayRowIndex=" + displayRowIndex +
                '}';
    }
}
