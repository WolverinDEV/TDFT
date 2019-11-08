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
                    } catch (Exception ex) {
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
                    if (!result.successfully()) {
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
            if (selectedPlugin == null) {
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
            if (selectedPlugin == null)
                return;
            Optional<Plugin> pluginOptional = this.pluginManager.loadedPlugins().stream().filter(e1 -> e1.getName().equals(selectedPlugin)).findFirst();
            if (!pluginOptional.isPresent()) {
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
        } catch (Exception ex) {
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

        for (Plugin plugin : pluginManager.loadedPlugins()) {
            model.addElement(plugin.getName());
        }
    }

    public void addPlugin(File plugin) {
        try {
            this.pluginManager.registerPlugin(plugin);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.mainPanel, "Failed to load plugin (" + plugin.getName() + "):\n" + ex.getMessage(), "Failed to load plugin", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            this.pluginManager.enablePlugin(plugin);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.mainPanel, "Failed to enable plugin (" + plugin.getName() + "):\n" + ex.getMessage(), "Failed to enable plugin", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.updatePluginList();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        Font mainPanelFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 14, mainPanel.getFont());
        if (mainPanelFont != null) mainPanel.setFont(mainPanelFont);
        final JSplitPane splitPane1 = new JSplitPane();
        mainPanel.add(splitPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(1000, 400), null, 1, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel1);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textAreaLog = new JTextArea();
        Font textAreaLogFont = this.$$$getFont$$$("Monospaced", Font.PLAIN, 12, textAreaLog.getFont());
        if (textAreaLogFont != null) textAreaLog.setFont(textAreaLogFont);
        scrollPane1.setViewportView(textAreaLog);
        testExecuteButton = new JPanel();
        testExecuteButton.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(5, 5, 5, 5), -1, -1));
        panel1.add(testExecuteButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        testExecuteButton.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        buttonStartStop = new JButton();
        buttonStartStop.setText("Execute tests");
        testExecuteButton.add(buttonStartStop, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelTestFeedback = new JLabel();
        labelTestFeedback.setText("You'll have some work to do");
        testExecuteButton.add(labelTestFeedback, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(5, 5, 5, 5), -1, -1));
        splitPane1.setLeftComponent(panel2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(-1, 20), null, null, 0, false));
        loadProjectButton = new JButton();
        loadProjectButton.setText("Load project");
        panel3.add(loadProjectButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(25, 20), null, new Dimension(200, -1), 0, false));
        labelSelectedFile = new JLabel();
        labelSelectedFile.setForeground(new Color(-8158333));
        labelSelectedFile.setText("No file selected");
        panel3.add(labelSelectedFile, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel3.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JTabbedPane tabbedPane1 = new JTabbedPane();
        panel2.add(tabbedPane1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Options", panel4);
        final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
        panel4.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        exitTestsOnFailitureCheckBox = new JCheckBox();
        exitTestsOnFailitureCheckBox.setText("Exit tests on failiture");
        panel4.add(exitTestsOnFailitureCheckBox, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        printFullStackTraceCheckBox = new JCheckBox();
        printFullStackTraceCheckBox.setText("Print full stack trace on error");
        panel4.add(printFullStackTraceCheckBox, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("Plugins (Tests)", panel5);
        final JLabel label1 = new JLabel();
        label1.setText("Plugin list");
        panel5.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setText("Add");
        panel5.add(addButton, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setText("Remove");
        panel5.add(removeButton, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pluginList = new JList();
        panel5.add(pluginList, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        labelProjectError = new JLabel();
        labelProjectError.setForeground(new Color(-4521972));
        labelProjectError.setText("An error");
        panel2.add(labelProjectError, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
