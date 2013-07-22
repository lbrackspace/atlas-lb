package org.rackspace.capman.tools.ca.gui.utils;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.rackspace.capman.tools.ca.StringUtils;

public class CaTextPane {

    private JTextPane textPane;
    private StyledDocument doc;
    private Style red;
    private Style green;
    private Style curStyle;

    public CaTextPane(JTextPane textPane) {
        this.textPane = textPane;
        this.doc = textPane.getStyledDocument();

        this.red = doc.addStyle("red", null);
        StyleConstants.setForeground(red, Color.red);

        this.green = doc.addStyle("green", null);
        StyleConstants.setForeground(green, Color.green);
    }

    public void write(String format, Object... args) {
        String msg = String.format(format, args);
        try {
            doc.insertString(doc.getLength(), msg, curStyle);
        } catch (BadLocationException ex) {
            Logger.getLogger(CaTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeException(Throwable ex) {
        String exceptionMessage = StringUtils.getEST(ex);
        Style origStyle = curStyle;
        curStyle = red;
        write(exceptionMessage);
        curStyle = origStyle;
    }

    public void redWrite(String fmt, Object... args) {
        Style origStyle = curStyle;
        curStyle = red;
        write(fmt, args);
        curStyle = origStyle;
    }

    public void greenWrite(String fmt, Object... args) {
        Style origStyle = curStyle;
        curStyle = green;
        write(fmt, args);
        curStyle = origStyle;
    }

    public void setRed() {
        this.curStyle = red;
    }

    public void setGreen() {
        this.curStyle = green;
    }

    public void clear() {
        textPane.setText("");
    }
}
