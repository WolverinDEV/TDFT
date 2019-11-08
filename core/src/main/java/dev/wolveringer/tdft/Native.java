package dev.wolveringer.tdft;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;

public class Native {
    private static final Logger logger = LoggerFactory.getLogger(Native.class);
    private static boolean supported = false;

    public static native void cd(String target);

    public static boolean available() {
        return supported;
    }

    public static void setup() {
        try {
            String filename;
            if(SystemUtils.IS_OS_WINDOWS) {
                String arch      = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
                boolean isOS64 = arch.endsWith("64") || (wow64Arch != null && wow64Arch.endsWith("64"));
                filename = "native_" + (isOS64 ? "64" : "32") + ".dll";
            } else {
                filename = "libnative.so";
            }

            try {
                System.load(new File("core/src/main/resources/" + filename).getAbsolutePath()); //Debug path
            } catch(Exception ex1) {
                URL resUri = Native.class.getResource("resources/" + filename);
                System.out.println(resUri);
            }
            supported = true;
        } catch(Exception ex) {
            logger.error("Failed to initialize native context. Native commands are not available!", ex);
        }
    }
}
