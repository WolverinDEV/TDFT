package dev.wolveringer.tdft;

import dev.wolveringer.tdft.gui.MainForm;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
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
        System.out.println("Hello World :)");

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
                            .required()
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

        if(cmd.hasOption("project"))
            mainForm.setProjectFile(new File(cmd.getOptionValue("project")));
        if(cmd.hasOption("plugin"))
            for(String plugin : cmd.getOptionValues("plugin"))
                mainForm.addPlugin(plugin);
    }
}
