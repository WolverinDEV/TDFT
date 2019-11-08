package dev.wolveringer.tdft;

import dev.wolveringer.tdft.gui.MainForm;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class GuiMain {
    private static final Logger logger = LoggerFactory.getLogger(GuiMain.class);

    private static class MessageWithLink extends JEditorPane {
        private static final long serialVersionUID = 1L;

        MessageWithLink(String html) {
            super("text/html", html);
            this.setOpaque(false);
            this.addHyperlinkListener(e -> {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException | URISyntaxException ex) {
                        logger.error("Failed to open link in HTML message", ex);
                    }
                }
            });
            this.setEditable(false);
            this.setBorder(null);
        }
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        //setUIFont (new javax.swing.plaf.FontUIResource("Consolas", Font.PLAIN,20));

        Options cliOptions = new Options();

        {
            cliOptions.addOption(
                    Option.builder("p")
                            .longOpt("project")
                            .optionalArg(true)
                            .hasArg()
                            .desc("The path to the exported Eclipse project you want to test")
                            .build()
            );

            cliOptions.addOption(
                    Option.builder("t")
                            .longOpt("plugin")
                            .optionalArg(true)
                            .hasArgs()
                            .desc("Specify plugins or full directories where the tester loads his tests from")
                            .build()
            );
        }

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("TDFT", cliOptions);

            System.exit(1);
        }

        MainForm mainForm = new MainForm();

        JFrame frame = new JFrame("TU-Darmstadt FOP Tester");
        frame.setContentPane(mainForm.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        {
            JMenuBar menu = new JMenuBar();

            JMenu about = new JMenu("About");
            {
                JMenuItem info = new JMenuItem("Info");
                info.addActionListener(e -> {
                    JOptionPane.showMessageDialog(frame, new MessageWithLink(
                            "<html>" +
                                    "TDFT - TU-Darmstadt FOP Tester version 1.0<br>" +
                                    "Writte by Markus Hadenfeldt<br>" +
                                    "Open source on github: <a href='https://github.com/WolverinDEV/TDFT'>github.com/WolverinDEV/TDFT</a>" +
                                    "</html>"), "Application info", JOptionPane.INFORMATION_MESSAGE);
                });
                about.add(info);

                JMenuItem contact = new JMenuItem("Contact");
                contact.addActionListener(e -> {
                    JOptionPane.showMessageDialog(frame, new MessageWithLink(
                            "<html>" +
                                    "You could contact me on moodle: <a href='https://moodle.informatik.tu-darmstadt.de/user/profile.php?id=13246'>here</a><br>" +
                                    "Or write me an EMail to: <a href='mailto:tdft@did.science'>tdft@did.science</a>" +
                                    "</html>"), "Contect the Developer", JOptionPane.INFORMATION_MESSAGE);
                });
                about.add(contact);
            }
            menu.add(about);

            frame.setJMenuBar(menu);
        }

        if(cmd.hasOption("project"))
            mainForm.setProjectFile(new File(cmd.getOptionValue("project")));
        if(cmd.hasOption("plugin"))
            for(String plugin : cmd.getOptionValues("plugin"))
                mainForm.addPlugin(new File(plugin));
    }
}
