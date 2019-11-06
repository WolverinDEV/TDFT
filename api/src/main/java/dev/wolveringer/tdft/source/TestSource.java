package dev.wolveringer.tdft.source;

import java.io.File;

public interface TestSource {
    void initialize() throws Exception;

    String getProjectName();

    File getWorkingDirectory();
    ClassLoader getClassLoader();
}
