package dev.wolveringer.tdft;

import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Helpers;

public interface TestContext {
    TestLogger getLogger();
    ClassLoader getClassLoader();
    Helpers getHelper();
    TestSource getSource();
}
