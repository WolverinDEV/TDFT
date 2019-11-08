package dev.wolveringer;

import dev.wolveringer.tdft.Native;
import dev.wolveringer.tdft.TestExecutor;
import dev.wolveringer.tdft.TestOptions;
import dev.wolveringer.tdft.TestResult;
import dev.wolveringer.tdft.unit.PluginManager;
import dev.wolveringer.tdft.source.EclipseProjectSource;
import dev.wolveringer.tdft.source.TestSource;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.File;
import java.text.DecimalFormat;

public class Main {
    private static final Logger logger;
    static {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "info");
        //System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
        System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "false");
        System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        System.setProperty("log4j.logger.org.xeustechnologies.*", "OFF");

        logger = LoggerFactory.getLogger(Main.class);
    }

    public static void main(String[] args) throws Exception {
        Native.setup();

        Options cliOptions = new Options();

        {
            cliOptions.addOption(
                    Option.builder("p")
                            .longOpt("project")
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
            formatter.printHelp("TDFT-Cli", cliOptions);

            System.exit(1);
        }

        TestSource source = new EclipseProjectSource(cmd.getOptionValue("project"));
        TestOptions options = new TestOptions();

        options.setExitOnFailure(false);
        options.setFullStackTrace(false);

        PluginManager unitManager = new PluginManager();

        for(String path : cmd.getOptionValues("plugin")) {
            logger.info("Adding plugin/plugin directory " + path);
            unitManager.registerPlugin(new File(path));
        }

        TestExecutor executor = new TestExecutor(source, unitManager, options);
        executor.initialize();

        TestResult result = executor.execute();
        result.print(System.out::println);

        if(!result.successfully()) {
            System.out.println("Test wasn't successfully!");
            System.out.println("I recommend to lookup your code and fix some bugs :)");
            System.exit(1);
        }

        System.out.println("All tests have been passed. Great!");
        System.out.println("Do you have any more ideas to test? May consider to contribute :)");
        System.exit(0);
    }
}
