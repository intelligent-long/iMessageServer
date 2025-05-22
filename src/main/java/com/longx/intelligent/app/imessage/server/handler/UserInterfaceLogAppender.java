package com.longx.intelligent.app.imessage.server.handler;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.longx.intelligent.app.imessage.server.ui.LogUi;
import org.springframework.stereotype.Component;

@Component
public class UserInterfaceLogAppender extends AppenderBase<ILoggingEvent> {
    private final PrintStream originalSystemOut = System.out;
    private final PrintStream originalSystemErr = System.err;


    public UserInterfaceLogAppender() {
        System.setOut(new PrintStream(new PrintToUserInterfaceStream()));
        System.setErr(new PrintStream(new PrintToUserInterfaceStream()));
    }

    @Override
    protected void append(ILoggingEvent event) {
        String formattedMessage = formatLoggingEvent(event);
        System.out.println(formattedMessage);
    }

    private String formatLoggingEvent(ILoggingEvent event) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(event.getTimeStamp()));
        String level = event.getLevel().toString();
        String threadName = event.getThreadName();
        String loggerName = event.getLoggerName();
        String message = event.getFormattedMessage();
        return String.format("[%s] %s --- [%s] %s : %s", timestamp, level, threadName, loggerName, message);
    }

    private static class PrintToUserInterfaceStream extends OutputStream {

        @Override
        public void write(int b) {
            LogUi.getInstance().append(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            String message = new String(b, off, len);
            LogUi.getInstance().append(message);
        }
    }

    public void restoreOriginalStreams() {
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }
}
