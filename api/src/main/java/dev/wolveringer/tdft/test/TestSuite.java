package dev.wolveringer.tdft.test;

import dev.wolveringer.tdft.TestContext;

@FunctionalInterface
public interface TestSuite {
    default void setup() {}
    default void cleanup() {}

    void run(TestContext context) throws Exception;
}
