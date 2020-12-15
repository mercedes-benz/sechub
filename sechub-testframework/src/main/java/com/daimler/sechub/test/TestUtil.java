// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SechubTestComponent
public class TestUtil {

    private static final OperationSystem operationSystem = new OperationSystem();

    public static String createRAndomString(int wantedLength) {
        if (wantedLength < 0) {
            throw new IllegalArgumentException("Length must be greater or equal 0!");
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length()<wantedLength) {
            sb.append(UUID.randomUUID().toString());
        }
        return sb.substring(0,wantedLength);
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

    public static boolean isDeletingTempFiles() {
        return !isKeepingTempfiles();
    }

    public static boolean isKeepingTempfiles() {
        if (Boolean.getBoolean(System.getenv("SECHUB_KEEP_TEMPFILES"))) {
            return true;
        }
        return false;
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
