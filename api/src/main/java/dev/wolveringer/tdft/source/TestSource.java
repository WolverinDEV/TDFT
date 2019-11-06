package dev.wolveringer.tdft.source;

import java.io.File;

public interface TestSource {
    /**
     * @apiNote Do not use this method! Its only for internal purposes!
     * @throws Exception An exception whill be thrown for any kind of error
     */
    void initialize() throws Exception;

    /**
     * @return The project name of the supplied project.
     *         E.g. H03_HADENFELDT_MARKUS
     */
    String getProjectName();

    /**
     * @return The required working directory to be able to execute the tests
     *         Usually this is also the current directory while executing the tests.
     */
    File getWorkingDirectory();

    /**
     * @see dev.wolveringer.tdft.TestContext getClassLoader
     * @return The class loader which should be used to execute all tests
     */
    ClassLoader getClassLoader();
}
