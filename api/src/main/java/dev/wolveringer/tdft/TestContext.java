package dev.wolveringer.tdft;

import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Helpers;

public interface TestContext {
    /**
     * @return A logger which should be used to output some messages, while executing the test
     */
    TestLogger getLogger();

    /**
     * @apiNote When loading classes be patient to ONLY load classes with this class loader
     * @return The class loader, which is capable of loading all required classes and libraries.
     */
    ClassLoader getClassLoader();

    /**
     * @return The method helper, usable for this test.
     */
    Helpers getHelper();

    /**
     * @return The source where the classes and resources are loaded from.
     *         Usually the supplied project to test
     */
    TestSource getSource();

    /**
     * @return The options the tests should respect.
     *         {@see TestOptions} for more detail
     */
    TestOptions getOptions();
}
