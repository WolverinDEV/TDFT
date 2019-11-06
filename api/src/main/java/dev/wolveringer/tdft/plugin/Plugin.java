package dev.wolveringer.tdft.plugin;

import dev.wolveringer.tdft.test.TestUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Plugin {
    private static final Logger logger = LoggerFactory.getLogger(Plugin.class);

    private Set<TestUnit> registeredTestUnits = new HashSet<>();

    protected Plugin() {}

    public abstract String getName();
    public abstract int getVersion();

    public void onEnable() {}
    public void onDisable() {}

    public Set<TestUnit> getRegisteredTestUnits() {
        return Collections.unmodifiableSet(registeredTestUnits);
    }

    public <T extends TestUnit> boolean registerTestUnit(Class<T> klass) {
        try {
            return this.registeredTestUnits.add(klass.newInstance());
        } catch(Exception ex) {
            logger.error("Failed to register a new test unit. The allocation of the given class failed", ex);
            return false;
        }
    }
}
