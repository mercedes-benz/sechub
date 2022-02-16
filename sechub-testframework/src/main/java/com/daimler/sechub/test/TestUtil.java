// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
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
     * 
     * @return false when environment variable
     *         {@value TestUtil#SECHUB_KEEP_TEMPFILES} is set to `true` otherwise
     *         true
     */
    public static boolean isDeletingTempFiles() {
        return !isKeepingTempfiles();
    }

    /**
     * Checks if temporary files are kept
     * 
     * @return true when environment variable
     *         {@value TestUtil#SECHUB_KEEP_TEMPFILES} is set to `true` otherwise
     *         false
     */
    public static boolean isKeepingTempfiles() {
        return Boolean.parseBoolean(System.getenv(SECHUB_KEEP_TEMPFILES));
    }

    /**
     * Creates a temporary file inside gradle build folder at
     * `./build/sechub/tmp/${prefix}_tmp_${nanoTime}.${fileEnding}`. When environment
     * entry `{@value TestUtil#SECHUB_KEEP_TEMPFILES}` is set to `true` those files
     * will be kept when JVM exits. Otherwise, those files will be deleted by JVM on
     * shutdown phase normally.
     * 
     * @param prefix filename prefix
     * @param fileEnding filename ending
     * @return file
     * @throws IOException
     */
    public static Path createTempFileInBuildFolder(String prefix, String fileEnding, FileAttribute<?>... attributes) throws IOException {
        return createTempFileInBuildFolder(prefix + "_tmp_" + System.nanoTime() + "." + fileEnding, attributes);
    }

    /**
     * Creates a temporary directory inside gradle build folder at
     * `./build/sechub/tmp/${dirName}_tmp_${nanoTime}`. When environment entry
     * `{@value TestUtil#SECHUB_KEEP_TEMPFILES}` is set to `true` those files will
     * be kept when JVM exits. Otherwise, those files will be deleted by JVM on
     * shutdown phase normally.
     * 
     * @param dirName
     * @return
     * @throws IOException
     */
    public static Path createTempDirectoryInBuildFolder(String dirName, FileAttribute<?>... attributes) throws IOException {
        Path tmpPath = ensureBuildTmpDirAsFile();

        Path dirAsPath = tmpPath.toRealPath().resolve(dirName + "tmp_" + System.nanoTime());
        if (Files.notExists(dirAsPath)) {
            Files.createDirectory(dirAsPath, attributes);

            if (isDeletingTempFiles()) {
                dirAsPath.toFile().deleteOnExit();
            }
        }
        return dirAsPath;
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
     * @throws IOException
     */
    public static Path createTempFileInBuildFolder(String explicitFileName, FileAttribute<?>... attributes) throws IOException {
        Path parent = ensureBuildTmpDirAsFile();
        Path filePath = parent.resolve(explicitFileName);

        if (Files.exists(filePath)) {
            LOG.warn("Temporary file already exists and will be deleted:{}", filePath);
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot delete former temp file", e);
            }
        }
        Files.createFile(filePath, attributes);
        if (isDeletingTempFiles()) {
            filePath.toFile().deleteOnExit();
        }
        return filePath;
    }

    private static Path ensureBuildTmpDirAsFile() throws IOException {
        File file = new File("./build/sechub/tmp");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException("Was not able to create tmp folder at:" + file.getAbsolutePath());
            }
        }
        return file.toPath().toRealPath();
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
