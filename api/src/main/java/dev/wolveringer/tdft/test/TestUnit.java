package dev.wolveringer.tdft.test;

import java.util.HashSet;
import java.util.Set;

import dev.wolveringer.tdft.TestContext;
import dev.wolveringer.tdft.TestLogger;

import dev.wolveringer.tdft.source.TestSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@RequiredArgsConstructor
public abstract class TestUnit {
    @AllArgsConstructor
    @Getter
    private static class TestSuiteData {
        private final TestSuite instance;
        private final String name;
    }

    private final String name;
    private Set<TestSuiteData> testSuites;

    public abstract boolean executable(TestSource source);
    protected abstract void registerTests(TestContext context);

    public void initialize(TestContext context) {
        Validate.isTrue(this.testSuites == null, "Unit already initialized");

        this.testSuites = new HashSet<>();
        this.registerTests(context);
    }

    public void cleanup() {
        this.testSuites = null;
    }

    public void initializeGlobalEnvironment() {}
    public void cleanupGlobalEnvironment() {}

    protected void registerTest(@NonNull TestSuite test) {
        this.registerTest(test, test.getClass().getName());
    }

    protected void registerTest(@NonNull TestSuite test, String name) {
        this.testSuites.add(new TestSuiteData(test, name));
    }

    public final boolean executeTests(@NonNull TestContext context) {
        TestLogger logger = context.getLogger();
        logger.pushContext(this.name);
        logger.info("Executing %d tests in this unit.", this.testSuites.size());

        try {
            for(TestSuiteData test : this.testSuites)
                if(!this.executeTest(context, test))
                    return false;
        } catch (Exception ex) {
            logger.fatal("executeTest() caused an exception. This shall not happen!", ex);
            return false;
        } finally {
            logger.popContext(this.name);
        }
        return true;
    }

    private boolean executeTest(@NonNull TestContext context, @NonNull TestSuiteData testSuite) {
        TestLogger logger = context.getLogger();
        logger.info("> Executing test suite \"" + testSuite.getName() + "\"");

        logger.pushContext(testSuite.getName());

        try {
            testSuite.getInstance().setup();
            testSuite.getInstance().run(context);
        } catch (Exception ex) {
            logger.fail("Test failed", ex);
            return false;
        } finally {
            try {
                testSuite.getInstance().cleanup();
            } catch(Exception ex) {
                logger.error("Suite caused an exception on cleanup!", ex);
            }
            logger.popContext(testSuite.getName());
        }
        logger.info("=> Test passed");
        return true;
    }
}
