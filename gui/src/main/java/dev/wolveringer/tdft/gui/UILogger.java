package dev.wolveringer.tdft.gui;

import dev.wolveringer.tdft.AbstractTestLogger;
import dev.wolveringer.tdft.TestExecutor;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class UILogger extends AbstractTestLogger {
    @NonNull private final JTextArea textArea;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    @Getter private PrintStream os = new PrintStream(bos);

    public UILogger(@NonNull TestExecutor handle, @NonNull JTextArea textArea) {
        super(handle);
        this.textArea = textArea;
    }

    public void updateText() {
        os.flush();
        textArea.setText(bos.toString());
    }


    @Override
    public void debug(String message, Object... arguments) {
        os.println("[Debug] " + String.format(message, arguments));
        this.updateText();
    }

    @Override
    public void info(String message, Object... arguments) {
        os.println("[Info] " + String.format(message, arguments));
        this.updateText();
    }

    @Override
    public void warning(String message, Object... arguments) {
        os.println("[Warning] " + String.format(message, arguments));
        this.updateText();
    }

    @Override
    public void error(String message, Object... arguments) {
        os.println("[Error] " + String.format(message, arguments));
        this.updateText();
    }

    @Override
    public void fatal(String message, Object... arguments) {
        os.println("[Fatal] " + String.format(message, arguments));
        this.updateText();
    }

    @Override
    public void fail(String message, Exception ex) {
        os.println("[Fatal] " + message);
        if(this.handle.getOptions().isFullStackTrace())
            ex.printStackTrace(os);
        else
            os.println("[Fatal] " + ex.getMessage());
        this.updateText();
    }
}
