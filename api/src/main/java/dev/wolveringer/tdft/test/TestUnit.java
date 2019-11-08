package dev.wolveringer.tdft.test;

import java.util.*;

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
    @Getter
    @RequiredArgsConstructor
    public static class Result {
        private final int testCount;
        private final int testsExecuted;
        private final int testsSucceeded;
        private final int testsSkipped;
    }

    private final String name;
    private Set<Test> testSuites;

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

    protected Test registerTest(@NonNull TestSuite test) {
        return this.registerTest(test, test.getClass().getName());
    }

    protected Test registerTest(@NonNull TestSuite test, String name) {
        Test result = new Test(test, name);
        this.testSuites.add(result);
        return result;
    }

    public final Result executeTests(@NonNull TestContext context) {
        TestLogger logger = context.getLogger();
        logger.pushContext(this.name);
        logger.info("Executing %d tests in this unit.", this.testSuites.size());

        List<Test> pendingTests = new ArrayList<>(this.testSuites);
        Map<String, TestState> executedTests = new HashMap<>();

        int testsTotal = pendingTests.size(), testsExecuted = 0, testsSucceeded = 0, testsSkipped = 0;
        while(!pendingTests.isEmpty()) {
            final int testCount = testsExecuted;

            tloop:
            for(Test test : new ArrayList<>(pendingTests)) {
                for(String depend : test.getRequiredTests()) {
                    TestState result = executedTests.getOrDefault(depend, TestState.PENDING);
                    if(result == TestState.PENDING)
                        continue tloop;

                    if(result == TestState.FAILED || result == TestState.SKIPPED) {
                        testsExecuted++;
                        testsSkipped++;

                        pendingTests.remove(test);
                        executedTests.put(test.getId(), TestState.SKIPPED);
                        test.setState(TestState.SKIPPED);

                        if(result == TestState.FAILED)
                            logger.info("> Skipping test " + test.getId() + " because required test previously failed (" + depend + ")");
                        else
                            logger.info("> Skipping test " + test.getId() + " because required test has been skipped (" + depend + ")");

                        continue tloop;
                    }
                }

                testsExecuted++;
                pendingTests.remove(test);
                this.executeTest(context, test);
                executedTests.put(test.getId(), test.getState());
                if(test.getState() == TestState.SUCCEEDED)
                    testsSucceeded++;
                else if(context.getOptions().isExitOnFailure()) {
                    break;
                }
            }

            /* Check if we haven't executed any tests */
            if(testCount == testsExecuted) {
                logger.error("Failed to execute all tests. May a circular dependency?");
                logger.debug("Tests left:");
                for(Test t : pendingTests)
                    logger.debug(" - " + t.getId());
                break;
            }
        }

        logger.popContext(this.name);
        return new Result(testsTotal, testsExecuted, testsSucceeded, testsSkipped);
    }

    private boolean executeTest(@NonNull TestContext context, @NonNull Test test) {
        TestLogger logger = context.getLogger();
        logger.info("> Executing test suite \"" + test.getId() + "\"");

        logger.pushContext(test.getId());

        try {
            test.getSuite().setup();
            test.getSuite().run(context);
            test.setState(TestState.SUCCEEDED);
        } catch (Exception ex) {
            test.setState(TestState.FAILED);
            logger.fail("Test failed", ex);
            return false;
        } finally {
            try {
                test.getSuite().cleanup();
            } catch(Exception ex) {
                logger.error("Suite caused an exception on cleanup!", ex);
            }
            logger.popContext(test.getId());
        }
        logger.info("=> Test passed");
        return true;
    }
}
