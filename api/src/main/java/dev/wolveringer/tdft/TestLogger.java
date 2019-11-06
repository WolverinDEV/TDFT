package dev.wolveringer.tdft;

public interface TestLogger {
    /**
     * @param name Push a new context on the "log stack"
     *             Every context could identity one unit
     */
    void pushContext(String name);
    /**
     * @param expectedName Pop the last context from the "log stack"
     *                     The last context name should be equal to the expected name, else you've messed something up
     */
    void popContext(String expectedName);

    void debug(String message, Object... arguments);
    void info(String message, Object... arguments);
    void warning(String message, Object... arguments);
    void error(String message, Object... arguments);
    void fatal(String message, Object... arguments);

    void fail(String message, Exception ex);
}
