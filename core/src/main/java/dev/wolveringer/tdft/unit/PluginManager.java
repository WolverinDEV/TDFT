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

    private List<TestPlugin> pluginInstances;
    private Set<File> pluginFiles = new HashSet<>();

    public Set<File> getPluginFiles() {
        return Collections.unmodifiableSet(this.pluginFiles);
    }

    public void registerPlugin(File path) {
        Validate.isTrue(this.pluginInstances == null, "Plugin manager has already been initialized!");
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
        }
        this.pluginFiles.add(path);
    }

    public boolean initialized() {
        return this.pluginInstances != null;
    }

    public void initialize() {
        Validate.isTrue(this.pluginInstances == null, "Plugin manager has already been initialized!");
        this.pluginInstances = new ArrayList<>();

        logger.debug("Loading " + this.pluginFiles.size() + " plugins.");
        for(File f : this.pluginFiles) {
            logger.trace("Loading plugin " + f);
            try {
                TestPlugin plugin = new TestPlugin(f, this.getClass().getClassLoader());
                plugin.load();
                this.pluginInstances.add(plugin);
            } catch (Exception ex) {
                logger.error("Failed to load plugin " + f + ". Ignoring test.", ex);
            }
        }
        logger.debug("Enabeling test plugins");
        for(TestPlugin tp : new ArrayList<>(this.pluginInstances)) {
            try {
                tp.getInstance().onEnable();
            } catch(Exception ex) {
                logger.warn("Failed to enable plugin " + tp.getInstance().getName(), ex);
                this.pluginInstances.remove(tp);
            }
        }
        logger.debug("Loaded " + this.pluginInstances.size() + " plugins and " + this.pluginInstances.stream().mapToInt(e -> e.getInstance().getRegisteredTestUnits().size()).sum() + " test units successfully.");
    }

    public List<Plugin> loadedPlugins() {
        return this.pluginInstances.stream().map(TestPlugin::getInstance).collect(Collectors.toList());
    }
}
