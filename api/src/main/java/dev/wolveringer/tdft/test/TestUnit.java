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

    public final boolean executeTests(@NonNull TestContext context) {
        TestLogger logger = context.getLogger();
        logger.pushContext(this.name);
        logger.info("Executing %d tests in this unit.", this.testSuites.size());

        List<Test> pendingTests = new ArrayList<>(this.testSuites);
        Map<String, TestState> executedTests = new HashMap<>();
        int numExecuted;
        while(!pendingTests.isEmpty()) {
            numExecuted = 0;

            tloop:
            for(Test test : new ArrayList<>(pendingTests)) {
                for(String depend : test.getRequiredTests()) {
                    TestState result = executedTests.getOrDefault(depend, TestState.PENDING);
                    if(result == TestState.PENDING)
                        continue tloop;

                    if(result == TestState.FAILED) {
                        numExecuted++;
                        pendingTests.remove(test);

                        logger.info("> Skipping test " + test.getId() + " because required test previously failed (" + depend + ")");
                        executedTests.put(test.getId(), TestState.SKIPPED);
                        test.setState(TestState.SKIPPED);
                        continue tloop;
                    }
                    if(result == TestState.SKIPPED) {
                        numExecuted++;
                        pendingTests.remove(test);

                        logger.info("> Skipping test " + test.getId() + " because required test has been skipped (" + depend + ")");
                        executedTests.put(test.getId(), TestState.SKIPPED);
                        test.setState(TestState.SKIPPED);
                        continue tloop;
                    }
                }

                numExecuted++;
                pendingTests.remove(test);
                this.executeTest(context, test);
                executedTests.put(test.getId(), test.getState());
            }

            if(numExecuted == 0) {
                logger.error("Failed to execute all tests. May a circular dependency?");
                logger.debug("Tests left:");
                for(Test t : pendingTests)
                    logger.debug(" - " + t.getId());
                break;
            }
        }

        logger.popContext(this.name);
        return true;
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
