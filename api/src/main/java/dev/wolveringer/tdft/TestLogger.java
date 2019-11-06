package dev.wolveringer.tdft;

public interface TestLogger {
    void pushContext(String name);
    void popContext(String expectedName);

    void debug(String message, Object... arguments);
    void info(String message, Object... arguments);
    void warning(String message, Object... arguments);
    void error(String message, Object... arguments);
    void fatal(String message, Object... arguments);

    void fail(String message, Exception ex);
}
