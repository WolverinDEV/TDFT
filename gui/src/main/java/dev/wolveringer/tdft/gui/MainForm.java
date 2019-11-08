package dev.wolveringer.tdft.gui;

import dev.wolveringer.tdft.TestExecutor;
import dev.wolveringer.tdft.TestOptions;
import dev.wolveringer.tdft.TestResult;
import dev.wolveringer.tdft.source.EclipseProjectSource;
import dev.wolveringer.tdft.unit.PluginManager;
import lombok.Getter;
import org.apache.commons.lang3.Validate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class MainForm {
    private JButton buttonStartStop;
    private JTextArea textAreaLog;
    private JButton loadProjectButton;
    @Getter
    private JPanel mainPanel;
    private JLabel labelSelectedFile;

    private EclipseProjectSource projectSource;
    private TestOptions options = new TestOptions();
    private PluginManager pluginManager = new PluginManager();

    public MainForm() {
        this.buttonStartStop.setEnabled(false);
        this.textAreaLog.setEditable(false);

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++)
            sb.append("Line " + i + "\n");
        this.textAreaLog.setText(sb.toString());

        this.loadProjectButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                int result = fc.showOpenDialog(MainForm.this.mainPanel);

                if (result == JFileChooser.APPROVE_OPTION) {
                    setProjectFile(fc.getSelectedFile());
                }
            }
        });

        this.buttonStartStop.addActionListener(e -> {
            this.loadProjectButton.setEnabled(false);
            this.buttonStartStop.setEnabled(false);

            Thread t = new Thread(() -> {
                try {
                    Validate.notNull(projectSource);
                    TestExecutor executor = new TestExecutor(projectSource, pluginManager, options);
                    UILogger logger = new UILogger(executor, textAreaLog);
                    executor.setTestLogger(logger);
                    try {
                        executor.initialize();
                    } catch(Exception ex) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ex.printStackTrace(new PrintStream(bos));
                        textAreaLog.setText(bos.toString());
                        return;
                    }

                    TestResult result = executor.execute();
                    result.print(m -> logger.getOs().println(m));
                    logger.updateText();

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    PrintStream pos = new PrintStream(bos);
                    if(!result.successfully()) {
                        pos.println("Test wasn't successfully!");
                        pos.println("I recommend to lookup your code and fix some bugs :)");
                    } else {
                        pos.println("All tests have been passed. Great!");
                        pos.println("Do you have any more ideas to test? May consider to contribute :)");
                    }
                    textAreaLog.append(bos.toString());
                } finally {
                    this.loadProjectButton.setEnabled(true);
                    this.buttonStartStop.setEnabled(true);
                }
            });
            t.start();
        });
    }

    public void setProjectFile(File target) {
        labelSelectedFile.setText(target.getName());
        textAreaLog.setText("");
        buttonStartStop.setEnabled(false);

        projectSource = new EclipseProjectSource(target.getAbsolutePath());
        try {
            projectSource.validate();
            buttonStartStop.setEnabled(true);
        } catch(Exception ex) {
            projectSource = null;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(bos));
            textAreaLog.setText(bos.toString());
        }
    }

    public void addPlugin(String path) {
        this.pluginManager.registerPlugin(new File(path));
    }
}
