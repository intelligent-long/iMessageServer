package com.longx.intelligent.app.imessage.server.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * Created by LONG on 2024/12/11 at 5:04 PM.
 */
public class RestrictiveDocument extends PlainDocument {
    private final JTextComponent textComponent;
    private int lineMax = 100;

    public RestrictiveDocument(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    public RestrictiveDocument(JTextComponent textComponent, int lineMax) {
        this.textComponent = textComponent;
        this.lineMax = lineMax;
    }

    public void insertString(int offset, String s, AttributeSet attributeSet) throws BadLocationException {
        String value = textComponent.getText();
        int overrun = 0;
        if (value != null && value.indexOf(' ') >= 0 && value.split(" ").length >= lineMax) {
            overrun = value.indexOf(' ') + 1;
            super.remove(0, overrun);
        }
        super.insertString(offset - overrun, s, attributeSet);
    }
}