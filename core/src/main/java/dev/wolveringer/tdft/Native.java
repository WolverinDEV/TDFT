package dev.wolveringer.tdft;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class Native {
    private static final Logger logger = LoggerFactory.getLogger(Native.class);
    private static boolean supported = false;

    public static native void cd(String target);

    public static boolean available() {
        return supported;
    }

    static {
        setup();
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
            } catch(UnsatisfiedLinkError ex1) {
                InputStream nativeIs = Native.class.getResourceAsStream("/resources/" + filename);
                if(nativeIs == null)
                    throw ex1;

                logger.debug("Found native addon within the jar resources. Extracting and loading it.");

                File file = File.createTempFile("_tdft_native", "");
                if(file.exists())
                    file.delete();
                Validate.isTrue(file.createNewFile(), "Failed to create new file");
                file.deleteOnExit();

                FileUtils.copyToFile(nativeIs, file);

                System.load(file.getAbsolutePath());
            }
            supported = true;
        } catch(Exception ex) {
            logger.error("Failed to initialize native context. Native commands are not available!", ex);
        }
    }
}
