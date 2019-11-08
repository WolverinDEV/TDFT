package dev.wolveringer.tdft.gui;

import dev.wolveringer.tdft.TestExecutor;
import dev.wolveringer.tdft.TestOptions;
import dev.wolveringer.tdft.TestResult;
import dev.wolveringer.tdft.plugin.Plugin;
import dev.wolveringer.tdft.source.EclipseProjectSource;
import dev.wolveringer.tdft.unit.PluginManager;
import dev.wolveringer.tdft.unit.TestPlugin;
import lombok.Getter;
import org.apache.commons.lang3.Validate;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Optional;

public class MainForm {
    private JButton buttonStartStop;
    private JTextArea textAreaLog;
    private JButton loadProjectButton;
    @Getter
    private JPanel mainPanel;
    private JLabel labelSelectedFile;
    private JLabel labelProjectError;
    private JCheckBox exitTestsOnFailitureCheckBox;
    private JCheckBox printFullStackTraceCheckBox;
    private JButton addButton;
    private JButton removeButton;
    private JList pluginList;
    private JPanel testExecuteButton;
    private JLabel labelTestFeedback;

    private EclipseProjectSource projectSource;
    private TestOptions options = new TestOptions();
    private PluginManager pluginManager = new PluginManager();

    public MainForm() {
        this.buttonStartStop.setEnabled(false);
        this.textAreaLog.setEditable(false);
        this.labelProjectError.setVisible(false);
        this.labelTestFeedback.setText("");

        {
            StringBuilder sb = new StringBuilder();
            sb.append("TDFT by Markus Hadenfeldt (2019)");
            this.textAreaLog.setText(sb.toString());
        }

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
                    this.testExecuteButton.setOpaque(false);

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
                        this.testExecuteButton.setBackground(new Color(255, 49, 30));
                        this.labelTestFeedback.setText("You'll have some work left.");
                    } else {
                        pos.println("All tests have been passed. Great!");
                        pos.println("Do you have any more ideas to test? May consider to contribute :)");
                        this.labelTestFeedback.setText("Great job! Seems like you're done!");
                        this.testExecuteButton.setBackground(new Color(132, 255, 118));
                    }
                    this.testExecuteButton.setOpaque(true);
                    this.testExecuteButton.repaint();
                    textAreaLog.append(bos.toString());
                } finally {
                    this.loadProjectButton.setEnabled(true);
                    this.buttonStartStop.setEnabled(true);
                }
            });
            t.start();
        });

        this.pluginList.setModel(new DefaultListModel());
        this.pluginList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.pluginList.addListSelectionListener(e -> {
            String selectedPlugin = (String) this.pluginList.getSelectedValue();
            if(selectedPlugin == null) {
                this.removeButton.setEnabled(false);
                return;
            }
            this.removeButton.setEnabled(true);
        });

        this.addButton.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            int result = fc.showOpenDialog(MainForm.this.mainPanel);

            if (result == JFileChooser.APPROVE_OPTION) {
                addPlugin(fc.getSelectedFile());
            }
        });

        this.removeButton.addActionListener(e -> {
            String selectedPlugin = (String) this.pluginList.getSelectedValue();
            if(selectedPlugin == null)
                return;
            Optional<Plugin> pluginOptional = this.pluginManager.loadedPlugins().stream().filter(e1 -> e1.getName().equals(selectedPlugin)).findFirst();
            if(!pluginOptional.isPresent()) {
                JOptionPane.showMessageDialog(this.mainPanel, "The requested plugin could not be found", "Failed to remove plugin", JOptionPane.ERROR_MESSAGE);
                return;
            }

            this.pluginManager.unregisterPlugin(pluginOptional.get());
            this.updatePluginList();
        });
        this.removeButton.setEnabled(false);

        this.exitTestsOnFailitureCheckBox.addItemListener(e -> {
            this.options.setExitOnFailure(this.exitTestsOnFailitureCheckBox.isSelected());
        });
        this.exitTestsOnFailitureCheckBox.setSelected(this.options.isExitOnFailure());

        this.printFullStackTraceCheckBox.addItemListener(e -> {
            this.options.setFullStackTrace(this.printFullStackTraceCheckBox.isSelected());
        });
        this.printFullStackTraceCheckBox.setSelected(this.options.isFullStackTrace());
    }

    public void setProjectFile(File target) {
        labelSelectedFile.setText(target.getName());
        textAreaLog.setText("");
        labelProjectError.setText("");
        labelProjectError.setVisible(false);
        buttonStartStop.setEnabled(false);

        projectSource = new EclipseProjectSource(target.getAbsolutePath());
        try {
            projectSource.validate();
            buttonStartStop.setEnabled(true);
        } catch(Exception ex) {
            projectSource = null;
            labelProjectError.setText(ex.getMessage());
            labelProjectError.setVisible(true);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ex.printStackTrace(new PrintStream(bos));
            textAreaLog.setText(bos.toString());
        }
    }

    private void updatePluginList() {
        DefaultListModel model = (DefaultListModel) this.pluginList.getModel();
        model.removeAllElements();

        for(Plugin plugin : pluginManager.loadedPlugins()) {
            model.addElement(plugin.getName());
        }
    }

    public void addPlugin(File plugin) {
        try {
            this.pluginManager.registerPlugin(plugin);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this.mainPanel, "Failed to load plugin (" + plugin.getName() + "):\n" + ex.getMessage(), "Failed to load plugin", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            this.pluginManager.enablePlugin(plugin);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this.mainPanel, "Failed to enable plugin (" + plugin.getName() + "):\n" + ex.getMessage(), "Failed to enable plugin", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.updatePluginList();
    }
}
