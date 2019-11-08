package dev.wolveringer.tdft;

import dev.wolveringer.tdft.plugin.Plugin;
import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Helpers;
import dev.wolveringer.tdft.test.TestUnit;
import dev.wolveringer.tdft.unit.PluginManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@RequiredArgsConstructor
@Getter
public class TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);

    private static class SimpleLogger extends AbstractTestLogger {
        private static final Logger logger = LoggerFactory.getLogger(SimpleLogger.class);

        public SimpleLogger(@NonNull TestExecutor handle) {
            super(handle);
        }

        @Override
        public void debug(String message, Object... arguments) {
            logger.debug(this.context + " " + String.format(message, arguments));
        }

        @Override
        public void info(String message, Object... arguments) {
            logger.info(this.context + " " + String.format(message, arguments));
        }

        @Override
        public void warning(String message, Object... arguments) {
            logger.info(this.context + String.format("[WARNING] " + message, arguments));
        }

        @Override
        public void error(String message, Object... arguments) {
            logger.error(this.context + " " + String.format(message, arguments));
        }

        @Override
        public void fatal(String message, Object... arguments) {
            logger.error(this.context + String.format("[FATAL] " + message, arguments));
        }

        @Override
        public void fail(String message, Exception ex) {
            if(this.handle.getOptions().isFullStackTrace()) {
                logger.error(this.context + "[FAIL] " + message, ex);
            } else {
                logger.error(this.context + "[FAIL] " + message + ":");
                logger.error(this.context + " " + ex.getMessage());
            }
        }
    }

    @NonNull private final TestSource source;
    @NonNull private final PluginManager unitManager;
    @NonNull private final TestOptions options;

    private TestContext context;
    @Setter private TestLogger testLogger;
    private Helpers helper;

    public void initialize() throws Exception {
        if(this.testLogger == null)
            this.testLogger = new SimpleLogger(this);

        try {
            this.unitManager.enableAllPlugins();
        } catch (Exception ex) {
            throw new Exception("failed to enable all plugins", ex);
        }

        try {
            if(!this.source.initialized())
                this.source.initialize();
        } catch (Exception ex) {
            throw new Exception("failed to initialize test source", ex);
        }
    }

    private TestContext getOrGenerateContext() {
        if(this.context != null) return this.context;
        return this.context = new TestContext() {
            @Override
            public TestLogger getLogger() {
                return testLogger;
            }

            @Override
            public ClassLoader getClassLoader() {
                return source.getClassLoader();
            }

            @Override
            public Helpers getHelper() {
                if(helper == null)
                    helper = new SimpleHelper(this);
                return helper;
            }

            @Override
            public TestSource getSource() {
                return source;
            }

            @Override
            public TestOptions getOptions() {
                return options;
            }
        };
    }

    public TestResult execute() {
        int testUnitsTotal = 0, testUnitsAvailable = 0;
        int testsAvailable = 0, testsExecuted = 0, testsSucceeded = 0, testsSkipped = 0;

        List<TestUnit> registeredTestUnits = new ArrayList<>();
        List<TestUnit> availableTestUnits = new ArrayList<>();

        this.testLogger.info("Loading tests...");
        for(Plugin tp : this.unitManager.loadedPlugins()) {
            logger.debug("Loading units for plugin " + tp.getName());

            Set<TestUnit> tests = tp.getRegisteredTestUnits();

            final int testUnits = testUnitsAvailable;
            final int testCount = testsAvailable;
            for(TestUnit unit : tests) {
                testUnitsTotal++;

                if(unit.executable(this.source)) {
                    testUnitsAvailable++;
                    try {
                        unit.initialize(this.getOrGenerateContext());
                        availableTestUnits.add(unit);
                        registeredTestUnits.add(unit);
                        testsAvailable += unit.getTestSuites().size();
                    } catch(Exception ex) {
                        unit.cleanup();
                        this.testLogger.error("Failed to initialize test unit " + unit.getName() + ". Ignoring unit.");
                        logger.error("Failed to initialize test unit " + unit.getName() + ". Ignoring unit.", ex);
                    }
                } else {
                    this.testLogger.debug("Skipping test unit " + unit.getName());
                    logger.trace("Skipping test unit " + unit.getName());
                }
            }

            logger.debug("Plugin " + tp.getName() + " scheduled " + (testUnitsAvailable - testUnits) + " test units with " + (testsAvailable - testCount) + " tests.");
            this.testLogger.info("Plugin " + tp.getName() + " scheduled " + (testUnitsAvailable - testUnits) + " test units with " + (testsAvailable - testCount) + " tests");
        }

        this.testLogger.info("Executing " + testsAvailable + " tests.");
        long begin = System.currentTimeMillis();

        logger.info("Found " + testUnitsAvailable + " test units with " + testsAvailable + " tests. Start testing....");
        for(TestUnit test : availableTestUnits) {
            try {
                test.initializeGlobalEnvironment();
            } catch(Exception ex) {
                logger.error("Failed to initialize global environment for test " + test.getName() + ". Test failed.", ex);
                break;
            }

            TestUnit.Result result = test.executeTests(this.getOrGenerateContext());
            testsExecuted += result.getTestsExecuted();
            testsSkipped += result.getTestsSkipped();
            testsSucceeded += result.getTestsSucceeded();
            if((result.getTestsExecuted() - result.getTestsSucceeded()) > 0) {
                if(this.getOptions().isExitOnFailure()) {
                    logger.info("Aborting tests because test unit " + test.getName() + " has some failed tests.");
                    break;
                }
            }

            try {
                test.cleanupGlobalEnvironment();
            } catch(Exception ex) {
                logger.error("Failed to cleanup global environment for test " + test.getName() + ". Ignoring this error.", ex);
            }
        }

        for(TestUnit unit : registeredTestUnits)
            unit.cleanup();

        this.testLogger.info("Executed " + testsExecuted + " tests in " + (System.currentTimeMillis() - begin) + "ms.");
        long now = System.currentTimeMillis();

        return new TestResult(
                testUnitsTotal,
                testUnitsAvailable,

                testsAvailable,
                testsExecuted,
                testsSucceeded,
                testsSkipped
        );
    }
}
