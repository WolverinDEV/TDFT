package dev.wolveringer.tdft.unit;

import dev.wolveringer.tdft.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PluginManager {
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private Map<File, TestPlugin> plugins = new HashMap<>();

    public Set<File> getPluginFiles() {
        return Collections.unmodifiableSet(this.plugins.keySet());
    }

    public void registerPlugin(File path) {
        if(this.plugins.containsKey(path))
            return;

        if(path.isFile()) {
            Validate.isTrue(path.canRead(), "Target file does not exists or isn't readable");
            Validate.isTrue(path.getName().endsWith(".jar"), "Target file must be a jar file");
        } else {
            Validate.isTrue(path.isDirectory(), "Path must be a file or directory");
            File[] files = path.listFiles();
            if(files == null) {
                logger.warn("Failed to iterate over plugin directory " + path);
                return;
            }

            for(File f : files) {
                if(f.isFile() && f.getName().endsWith(".jar"))
                    this.registerPlugin(f);
            }
            return;
        }

        this.plugins.put(path, null);
    }

    public void unregisterPlugin(Plugin p) {
        Optional<Map.Entry<File, TestPlugin>> plugin = this.plugins.entrySet().stream().filter(e -> e.getValue() != null && e.getValue().getInstance() == p).findFirst();
        if(!plugin.isPresent())
            return;

        this.disablePlugin(plugin.get().getKey());
        this.plugins.remove(plugin.get().getKey());
    }

    public void enableAllPlugins() {
        logger.debug("Enabling " + this.plugins.entrySet().stream().filter(e -> e.getValue() == null).count() + " new plugins.");
        for(Map.Entry<File, TestPlugin> plData : this.plugins.entrySet()) {
            if(plData.getValue() == null)
                this.enablePlugin(plData.getKey());

        }

        List<TestPlugin> plugins = this.plugins.entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getValue).collect(Collectors.toList());
        logger.debug("Loaded " + this.plugins.size() + " plugins and " + plugins.stream().mapToInt(e -> e.getInstance().getRegisteredTestUnits().size()).sum() + " test units successfully.");
    }

    public boolean enablePlugin(File pluginFile) {
        Optional<Map.Entry<File, TestPlugin>> pl = this.plugins.entrySet().stream().filter(e -> e.getKey() == pluginFile).findFirst();
        Validate.isTrue(pl.isPresent(), "Plugin not registered!");
        if(pl.get().getValue() != null)
            return true;

        logger.trace("Loading plugin " + pluginFile);
        try {
            TestPlugin plugin = new TestPlugin(pluginFile, this.getClass().getClassLoader());
            plugin.load();
            plugin.getInstance().onEnable();
            this.plugins.put(pluginFile, plugin);
        } catch (Exception ex) {
            logger.error("Failed to load plugin " + pluginFile + ". Ignoring test.", ex);
            return false;
        }
        return true;
    }

    public boolean disablePlugin(File pluginFile) {
        Optional<Map.Entry<File, TestPlugin>> pl = this.plugins.entrySet().stream().filter(e -> e.getKey() == pluginFile).findFirst();
        Validate.isTrue(pl.isPresent(), "Plugin not registered!");

        TestPlugin plugin = pl.get().getValue();
        plugin.getInstance().onDisable();

        this.plugins.put(pluginFile, null);
        return true;
    }

    public List<Plugin> loadedPlugins() {
        return this.plugins.values().stream().filter(Objects::nonNull).map(TestPlugin::getInstance).collect(Collectors.toList());
    }
}
