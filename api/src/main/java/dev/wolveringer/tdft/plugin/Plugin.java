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

    /**
     * @return This should return the name of the given plugin
     */
    public abstract String getName();

    /**
     * @return The version number of this plugin.
     * @apiNote This is currently not used, but for future updates
     */
    public abstract int getVersion();

    /**
     * This method will be called as soon the plugin has been loaded.
     * Within this method you should register your test units
     */
    public void onEnable() {}

    /**
     * This method will be called as soon the plugin gets unloaded.
     * @apiNote Currently never called!
     */
    public void onDisable() {}

    /**
     * @return A unmodifiable set of all currently registered test units
     */
    public Set<TestUnit> getRegisteredTestUnits() {
        return Collections.unmodifiableSet(registeredTestUnits);
    }

    /**
     * @param klass The unit. The class must inherit TestUnit
     * @return Wherever the unit has been registered successfully
     */
    protected  <T extends TestUnit> boolean registerTestUnit(Class<T> klass) {
        try {
            return this.registeredTestUnits.add(klass.newInstance());
        } catch(Exception ex) {
            logger.error("Failed to register a new test unit. The allocation of the given class failed", ex);
            return false;
        }
    }
}
