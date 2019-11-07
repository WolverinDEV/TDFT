package dev.wolveringer.tdft;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Native {
    private static final Logger logger = LoggerFactory.getLogger(Native.class);
    private static boolean supported = false;

    public static native void cd(String target);

    public static boolean available() {
        return supported;
    }

    public static void setup() {
        try {
            if(SystemUtils.IS_OS_WINDOWS) {
                String arch      = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
                boolean isOS64 = arch.endsWith("64") || (wow64Arch != null && wow64Arch.endsWith("64"));
                System.load(new File("core/src/main/resources/native_" + (isOS64 ? "64" : "32") + ".dll").getAbsolutePath());
            } else {
                System.load(new File("core/src/main/resources/libnative.so").getAbsolutePath());
            }
            supported = true;
        } catch(Exception ex) {
            logger.error("Failed to initialize native context. Native commands are not available!", ex);
        }
    }
}
