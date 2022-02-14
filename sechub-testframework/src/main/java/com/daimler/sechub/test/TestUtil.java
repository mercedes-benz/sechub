// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SechubTestComponent
public class TestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(TestUtil.class);

    private static final String SECHUB_KEEP_TEMPFILES = "SECHUB_KEEP_TEMPFILES";
    private static final OperationSystem operationSystem = new OperationSystem();

    public static String createRAndomString(int wantedLength) {
        if (wantedLength < 0) {
            throw new IllegalArgumentException("Length must be greater or equal 0!");
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < wantedLength) {
            sb.append(UUID.randomUUID().toString());
        }
        return sb.substring(0, wantedLength);
    }

    /**
     * Tries to get system property value
     * 
     * @param key
     * @return value, never <code>null</code>
     * @throws IllegalStateException when no system property can be resolved
     */
    public static String getSystemProperty(String key) {
        return getSystemProperty(key, null);
    }

    /**
     * Tries to get system property value
     * 
     * @param key
     * @param defaultValue value to use when system property is not found
     * @return value, never <code>null</code>
     * @throws IllegalStateException when no system property can be resolved -
     *                               happens only when default value is set to
     *                               <code>null</code>
     */
    public static String getSystemProperty(String key, String defaultValue) {
        String property = System.getProperty(key);
        if (property == null) {
            property = defaultValue;
        }
        if (property == null) {
            throw new IllegalStateException("System property with key:" + key + " not set!");
        }
        return property;
    }

    /**
     * Tries to get system property value as boolean value. If not defined
     * <code>false</code> will be returned!
     * 
     * @param key
     * @return defined boolean value or <code>false</code> if not defined
     */
    public static boolean getSystemPropertyBooleanOrFalse(String key) {
        String property = System.getProperty(key);
        return Boolean.parseBoolean(property);
    }

    public static void dumpSystemProperty(String key) {
        System.out.println("property:" + key + "=" + getSystemProperty(key, "<NOT DEFINED/>"));
    }

    public static void waitMilliseconds(long milliseconds) {
        try {
            Thread.sleep(milliseconds); // NOSONAR
        } catch (InterruptedException e) {
            throw new IllegalStateException("Testcase szenario corrupt / should not happen", e);
        }

    }

    /**
     * Checks if temporary files are deleted
     * @return false when environment variable {@value TestUtil#SECHUB_KEEP_TEMPFILES} is set to `true` otherwise true
     */
    public static boolean isDeletingTempFiles() {
        return !isKeepingTempfiles();
    }

    /**
     * Checks if temporary files are kept
     * @return true when environment variable {@value TestUtil#SECHUB_KEEP_TEMPFILES} is set to `true` otherwise false
     */
    public static boolean isKeepingTempfiles() {
        return Boolean.parseBoolean(System.getenv(SECHUB_KEEP_TEMPFILES));
    }

    /**
     * Creates a temporary file inside gradle build folder at
     * `./build/sechub/tmp/**`. When environment entry
     * `{@value TestUtil#SECHUB_KEEP_TEMPFILES}` is set to `true` those files will
     * be kept when JVM exits. Otherwise, those files will be deleted by JVM on
     * shutdown phase normally.
     * 
     * @param prefix filename prefix
     * @param suffix filename suffix
     * @return file
     */
    public static File createTempFileInBuildFolder(String prefix, String suffix) {
        return createTempFileInBuildFolder(prefix + "_tmp_" + System.nanoTime() + "." + suffix);
    }

    /**
     * Creates a temporary file inside gradle build folder at
     * `./build/sechub/tmp/**`. When environment entry
     * `{@value TestUtil#SECHUB_KEEP_TEMPFILES}` is set to `true` those files will
     * be kept when JVM exits. Otherwise, those files will be deleted by JVM on
     * shutdown phase normally.
     * 
     * If a file already exists with the given name, the file be deleted!
     * 
     * @param explicitFileName the EXACT file name to use.
     * 
     * @return file
     */
    public static File createTempFileInBuildFolder(String explicitFileName) {
        File parent = new File("./build/sechub/tmp");
        File file = new File(parent, explicitFileName);
        if (file.exists()) {
            LOG.warn("Temporary file already exists and will be deleted:{}",file.getAbsolutePath());
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Cannot delete former temp file",e);
            }
        }
        if (isDeletingTempFiles()) {
            file.deleteOnExit();
        }
        return file;
    }

    public static boolean isWindows() {
        return operationSystem.isWindows();
    }

    public static void unzip(final File zipFile, final Path unzipTo) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = unzipTo.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else {
                    // just ensure parent files are really available...
                    toPath.toFile().getParentFile().mkdirs();
                    Files.copy(zipInputStream, toPath);
                }
            }
        }
    }

    private static class OperationSystem {

        private boolean windows;

        OperationSystem() {
            String os = System.getProperty("os.name").toLowerCase();
            ;
            windows = (os.indexOf("win") >= 0);
        }

        public boolean isWindows() {
            return windows;
        }
    }

}
