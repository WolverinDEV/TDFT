package dev.wolveringer.tdft.test;

import dev.wolveringer.tdft.TestContext;

@FunctionalInterface
public interface TestSuite {
    /**
     * This method will be called every time before run will be executed
     */
    default void setup() {}

    /**
     * This method will be called every time after run has been executed.
     * Even if run(...) throws an exception
     */
    default void cleanup() {}

    /**
     * @param context The context where the tests should be applied on
     * @throws Exception Throws an exception as soon something does not work like it should be
     */
    void run(TestContext context) throws Exception;
}
