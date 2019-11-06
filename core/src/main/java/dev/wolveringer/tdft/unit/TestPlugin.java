package dev.wolveringer.tdft.unit;

import dev.wolveringer.tdft.plugin.Plugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Getter
@RequiredArgsConstructor
public class TestPlugin {
    private static final Logger logger = LoggerFactory.getLogger(TestPlugin.class);

    private final File pluginFile;
    private final ClassLoader rootClassLoader;

    private HashMap<String, String> manifest;

    private URLClassLoader pluginClassLoader;
    private Plugin instance;

    public void load() throws Exception {
        this.manifest = null;

        ZipFile file = new ZipFile(this.pluginFile);
        try {
            Enumeration<? extends ZipEntry> e = file.entries();
            while(e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                if(!entry.getName().equals("META-INF/MANIFEST.MF"))
                    continue;

                for(String line : IOUtils.toString(file.getInputStream(entry), Charset.defaultCharset()).split("\n")) {
                    int index = line.indexOf(':');
                    if(index == -1) {
                        if(!line.trim().isEmpty())
                            logger.warn("Invalid manifest entry: " + line);
                        continue;
                    }

                    if(this.manifest == null)
                        this.manifest = new HashMap<>();

                    this.manifest.put(line.substring(0, index).trim(), line.substring(index + 1).trim());
                }
            }
            Validate.notNull(this.manifest, "Failed to get manifest attributes");
            Validate.isTrue(this.manifest.get("pluginClass") != null, "Missing plugin class attribute");
        } finally {
            file.close();
        }

        this.pluginClassLoader = new URLClassLoader(new URL[]{this.pluginFile.toURI().toURL()}, this.rootClassLoader);
        Class<Plugin> pluginClass;
        try {
            pluginClass = (Class<Plugin>) this.pluginClassLoader.loadClass(this.manifest.get("pluginClass"));
            Validate.notNull(pluginClass);
        } catch(Exception ex) {
            throw new Exception("Failed to find main class (" + this.manifest.get("pluginClass") + ")", ex);
        }

        try {
            this.instance = pluginClass.newInstance();
        } catch (Exception ex) {
            throw new Exception("Failed to create a new instance of the test plugin", ex);
        }
    }
}
