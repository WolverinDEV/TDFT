package dev.wolveringer;

import dev.wolveringer.tdft.TestExecutor;
import dev.wolveringer.tdft.TestResult;
import dev.wolveringer.tdft.unit.PluginManager;
import dev.wolveringer.tdft.source.EclipseProjectSource;
import dev.wolveringer.tdft.source.TestSource;
import org.slf4j.impl.SimpleLogger;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "info");
        System.setProperty(SimpleLogger.SHOW_LOG_NAME_KEY, "false");
        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "false");
        System.setProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        System.setProperty("log4j.logger.org.xeustechnologies.*", "OFF");

        System.load(new File("core/src/main/resources/libnative.so").getAbsolutePath());


        TestSource source = new EclipseProjectSource("/home/wolverindev/Downloads/H03_Hadenfeldt_Markus.zip");
        PluginManager unitManager = new PluginManager();

        unitManager.registerPlugin(new File("tests/h3/target/h3-1.0.jar"));

        TestExecutor executor = new TestExecutor(source, unitManager);
        executor.initialize();

        TestResult result = executor.execute();
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
