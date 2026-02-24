package com.longx.intelligent.app.imessage.server.handler;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.longx.intelligent.app.imessage.server.ui.LogUi;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserInterfaceLogAppender extends AppenderBase<ILoggingEvent> {
    private static final boolean HEADLESS = GraphicsEnvironment.isHeadless();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    public UserInterfaceLogAppender() {
        if (!HEADLESS) {
            System.setOut(new PrintStream(new PrintToUserInterfaceStream(), true));
            System.setErr(new PrintStream(new PrintToUserInterfaceStream(), true));
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (HEADLESS) return;

        LogUi ui = LogUi.getIfExists();
        if (ui != null) {
            String text = formatLoggingEvent(event) + "\n";
            SwingUtilities.invokeLater(() -> ui.append(text));
        }
    }

    private String formatLoggingEvent(ILoggingEvent event) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .format(new Date(event.getTimeStamp()));
        String level = event.getLevel().toString();
        String threadName = event.getThreadName();
        String loggerName = event.getLoggerName();
        String message = event.getFormattedMessage();

        return String.format("[%s] %s --- [%s] %s : %s",
                timestamp, level, threadName, loggerName, message);
    }

    private static class PrintToUserInterfaceStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void write(int b) {
            char c = (char) b;
            buffer.append(c);

            if (c == '\n') {
                flushBuffer();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) {
            String s = new String(b, off, len);
            buffer.append(s);

            if (s.contains("\n")) {
                flushBuffer();
            }
        }

        private void flushBuffer() {
            if (HEADLESS) return;

            LogUi ui = LogUi.getIfExists();
            if (ui != null && buffer.length() > 0) {
                String text = buffer.toString();
                buffer.setLength(0);

                SwingUtilities.invokeLater(() -> ui.append(text));
            }
        }
    }

    public void restoreOriginalStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}