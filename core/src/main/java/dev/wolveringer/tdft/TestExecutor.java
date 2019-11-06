package dev.wolveringer.tdft;

import dev.wolveringer.tdft.plugin.Plugin;
import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Helpers;
import dev.wolveringer.tdft.test.TestUnit;
import dev.wolveringer.tdft.unit.PluginManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public class TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);

    private static class SimpleLogger implements TestLogger {
        private static final Logger logger = LoggerFactory.getLogger(SimpleLogger.class);

        private List<String> contextStack = new ArrayList<>();
        private String context = "";

        private void generateContext() {
            if(this.contextStack.isEmpty())
                this.context = "";
            else
                this.context = this.contextStack.stream().collect(Collectors.joining("::", "[", "]"));
        }

        @Override
        public void pushContext(String name) {
            this.contextStack.add(name);
            this.generateContext();
        }

        @Override
        public void popContext(String expectedName) {
            Validate.isTrue(!this.context.isEmpty(), "Context stack is empty");
            String givenName = this.contextStack.remove(this.contextStack.size() - 1);
            this.generateContext();

            Validate.isTrue(givenName.equals(expectedName), "Expected context name is not equals to the given name (" + givenName +" != " + expectedName + ")");
        }

        @Override
        public void debug(String message, Object... arguments) {
            logger.debug(this.context + " " + String.format(message, arguments));
        }

        @Override
        public void info(String message, Object... arguments) {
            logger.info(this.context + " " + String.format(message, arguments));
        }

        @Override
        public void warning(String message, Object... arguments) {
            logger.info(this.context + String.format("[WARNING] " + message, arguments));
        }

        @Override
        public void error(String message, Object... arguments) {
            logger.error(this.context + " " + String.format(message, arguments));
        }

        @Override
        public void fatal(String message, Object... arguments) {
            logger.error(this.context + String.format("[FATAL] " + message, arguments));
        }

        @Override
        public void fail(String message, Exception ex) {
            logger.error(this.context + "[FAIL] " + message, ex);
        }
    }

    @RequiredArgsConstructor
    @Getter
    private static class SimpleHelper implements Helpers {
        @NonNull private final TestContext context;

        @Override
        public <T> Class<T> resolveClass(@NonNull String path) {
            try {
                return (Class<T>) Class.forName(path, true, context.getClassLoader());
            } catch(Exception ex) {
                throw new RuntimeException("Failed to resolve class " + path + ". Maybe missing?", ex);
            }
        }

        @Override
        public <T> T createInstance(String klass, Object... arguments) {
            return createInstance(this.resolveClass(klass), arguments);
        }

        @Override
        public <T> T createInstance(Class<T> klass, Object... arguments) {
            if(arguments.length != 0)
                throw new RuntimeException("TODO: Implement me!");

            //TODO: Test if constructor exists and if its accessible
            try {
                return klass.newInstance();
            } catch(Exception ex) {
                throw new RuntimeException("Failed to create a new instance of class " + klass.getName() + ".", ex);
            }
        }

        private boolean similarParameters(Class<?> a, Class<?> b) {
            if(a.isArray() || b.isArray()) {
                if(!a.isArray() || !b.isArray())
                    return false;
                return similarParameters(a.getComponentType(), b.getComponentType());
            } else if(a == b)
                return true;
            else if(byte.class == a)
                return similarParameters(b, Byte.class);
            else if(char.class == a)
                return similarParameters(b, Character.class);
            else if(short.class == a)
                return similarParameters(b, Short.class);
            else if(int.class == a)
                return similarParameters(b, Integer.class);
            else if(long.class == a)
                return similarParameters(b, Long.class);
            else if(double.class == a)
                return similarParameters(b, Double.class);
            else if(float.class == a)
                return similarParameters(b, Float.class);
            else if(b.isPrimitive())
                return similarParameters(b, a);
            return false;
        }

        private String describeMethod(String name, int modifiers, Class<?> result, Class<?>... arguments) {
            StringBuilder str = new StringBuilder();

            if(modifiers > 0)
                str.append(Modifier.toString(modifiers));
            else
                str.append("public");
            str.append(" ");
            if(result == void.class)
                str.append("void");
            else
                str.append(result.getSimpleName());
            str.append(" ");
            str.append(name);
            str.append("(");
            for(int i = 0; i < arguments.length; i++) {
                str.append(arguments[i].getSimpleName());
                str.append(" a");
                str.append(i);
                if(i + 1 < arguments.length)
                    str.append(", ");
            }
            str.append(")");

            return str.toString();
        }

        @Override
        public <T> Method resolveMethod(@NonNull Class<T> klass, @NonNull String name, int modifiers, Class<?> result, Class<?>... arguments) {
            Optional<Method> method;

            /* resolve the method */
            try {
                method = Stream.of(
                        klass.getMethods(),
                        klass.getDeclaredMethods()
                ).flatMap(Stream::of)
                        .filter(e -> e.getName().equals(name))
                        .filter(e -> similarParameters(e.getReturnType(), result))
                        .filter(e -> e.getParameterCount() == arguments.length)
                        .filter(e -> {
                            Class<?>[] paramTypes = e.getParameterTypes();
                            for(int i = 0; i < paramTypes.length; i++)
                                if(!similarParameters(paramTypes[i], arguments[i]))
                                    return false;
                            return true;
                        }).findFirst();
            } catch(Exception ex) {
                throw new RuntimeException("failed resolve method " + name, ex);
            }
            Method m = method.orElseThrow(() -> new RuntimeException("failed to resolve method \"" + describeMethod(name, modifiers, result, arguments) + "\" in " + klass.getName()));

            /* validate arguments */
            if(modifiers >= 0) {
                Validate.isTrue((m.getModifiers() & modifiers) == modifiers, "Required modifiers for method " + name + " in class " + klass.getName() + " are not given (required: " + Modifier.toString(modifiers) + "; given: " + Modifier.toString(m.getModifiers()) + ")");
            }

            return m;
        }

        @Override
        public <T> void executeWithExpect(@NonNull Object obj, @NonNull String name, int modifiers, T result, Comparator<T> resultCmp, Object... arguments) {
            Class<?>[] args = Stream.of(arguments).map(Object::getClass).toArray(Class<?>[]::new);
            Method m = this.resolveMethod(obj.getClass(), name, modifiers, result == Void.TYPE ? void.class : result.getClass(), args);

            this.executeWithExpect(obj, m, result, resultCmp, arguments);
        }

        @Override
        public <T> void executeWithExpect(@NonNull Object obj, @NonNull Method method, T result, Comparator<T> resultCmp, Object... arguments) {
            Object res;
            try {
                res = method.invoke(obj, arguments);
            } catch(Exception ex) {
                throw new RuntimeException("failed to invoke method " + method.getName(), ex);
            }

            if(result != Void.TYPE) {
                T cr;
                try {
                    cr = (T) res;
                } catch(ClassCastException ex) {
                    throw new RuntimeException("Result is not the expected type!", ex);
                }
                if(resultCmp.compare(result, cr) != 0) {
                    throw new RuntimeException("received result isn't equal to the expected.\nExpected: " + result + "\nReceived: " + cr);
                }
            }
        }
    }

    @NonNull private final TestSource source;
    @NonNull private final PluginManager unitManager;

    private TestContext context;
    private TestLogger testLogger;
    private Helpers helper;

    public void initialize() throws Exception {
        this.testLogger = new SimpleLogger();

        try {
            this.unitManager.initialize();
        } catch (Exception ex) {
            throw new Exception("failed to initialize test unit manager", ex);
        }

        try {
            this.source.initialize();
        } catch (Exception ex) {
            throw new Exception("failed to initialize test source", ex);
        }
    }

    private TestContext getOrGenerateContext() {
        if(this.context != null) return this.context;
        return this.context = new TestContext() {
            @Override
            public TestLogger getLogger() {
                return testLogger;
            }

            @Override
            public ClassLoader getClassLoader() {
                return source.getClassLoader();
            }

            @Override
            public Helpers getHelper() {
                if(helper == null)
                    helper = new SimpleHelper(this);
                return helper;
            }

            @Override
            public TestSource getSource() {
                return source;
            }
        };
    }

    public TestResult execute() {
        int testsTotal = 0, testsExecuted = 0, testsPassed = 0;

        List<TestUnit> availableTests = new ArrayList<>();
        for(Plugin tp : this.unitManager.loadedPlugins()) {
            logger.debug("Loading units for plugin " + tp.getName());
            int savedTestsTotal = testsTotal;

            Set<TestUnit> tests = tp.getRegisteredTestUnits();
            for(TestUnit unit : tests) {
                if(unit.executable(this.source)) {
                    testsTotal++;
                    try {
                        unit.initialize(this.getOrGenerateContext());
                        availableTests.add(unit);
                    } catch(Exception ex) {
                        unit.cleanup();
                        logger.error("Failed to initialize test unit " + unit.getName() + ". Ignoring unit.");
                        continue;
                    }
                } else {
                    logger.trace("Skipping test unit " + unit.getName());
                }
            }

            logger.debug("Plugin " + tp.getName() + " scheduled " + (testsTotal - savedTestsTotal) + " tests.");
        }

        logger.info("Found " + testsTotal + " test units. Start testing....");
        for(TestUnit test : availableTests) {
            testsExecuted++;
            try {
                test.initializeGlobalEnvironment();
            } catch(Exception ex) {
                logger.error("Failed to initialize global environment for test " + test.getName() + ". Test failed.", ex);
                break;
            }
            if(test.executeTests(this.getOrGenerateContext())) {
                testsPassed++;
                try {
                    test.initializeGlobalEnvironment();
                } catch(Exception ex) {
                    logger.error("Failed to cleanup global environment for test " + test.getName() + ". Ignoring this error.", ex);
                }
                continue;
            }

            break;
        }

        return new TestResult(-1, testsTotal, testsExecuted, testsPassed);
    }
}
