// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SechubTestComponent
public class TestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(TestUtil.class);

    private static final String SECHUB_KEEP_TEMPFILES = "SECHUB_KEEP_TEMPFILES";
    private static final String SECHUB_TEST_TRACEMODE = "SECHUB_TEST_TRACEMODE";

    private static Set<Path> currentTempFolders = new LinkedHashSet<>();

    public static void deleteAllCurrentCreatedTempFolders() {
        if (!isDeletingTempFiles()) {
            return;
        }
        if (currentTempFolders.isEmpty()) {
            return;
        }

        /* try to delete output folder paths */
        synchronized (currentTempFolders) {

            Set<Path> deletedTempFolders = new LinkedHashSet<>();
            for (Path tempFolder : currentTempFolders) {
                try {
                    if (Files.exists(tempFolder)) {
                        LOG.trace("Delete temp directory: {}", tempFolder);
                        PathUtils.deleteDirectory(tempFolder);
                    }
                } catch (Exception e) {

                    LOG.error("Was not able to delete temp folder: {}. Reason: {}", tempFolder, e.getMessage());

                } finally {

                    deletedTempFolders.add(tempFolder);
                }
            }
            currentTempFolders.removeAll(deletedTempFolders);
        }
    }

    public static <E extends Exception, R> FailUntilAmountOfRunsReached<E, R> createFailUntil(int amount, E failure, R result) {
        return new FailUntilAmountOfRunsReached<>(amount, failure, result);
    }

    public static <E extends Exception, R> FailUntilAmountOfRunsReached<E, R> createFailUntil(int amount, Class<E> failure, R result) {
        return new FailUntilAmountOfRunsReached<>(amount, failure, result);
    }

    public static <E extends Exception> FailUntilAmountOfRunsReached<E, Void> createFailUntil(int amount, E failure) {
        return new FailUntilAmountOfRunsReached<>(amount, failure, (Void) null);
    }

    public static <E extends Exception> FailUntilAmountOfRunsReached<E, Void> createFailUntil(int amount, Class<E> failure) {
        return new FailUntilAmountOfRunsReached<>(amount, failure, (Void) null);
    }

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

    public static int getSystemPropertyIntOrDefault(String id, int defaultValue) {
        String sechubPortAsString = System.getProperty(id);

        int value = defaultValue;
        try {
            value = Integer.parseInt(sechubPortAsString);
        } catch (NumberFormatException e) {
            /* ignore - we use default */
        }
        return value;
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
     * In some situations a developer wants to enable special tracing (without
     * changing any log levels etc.). Dedicated points in test code can use this
     * method to log more information in this case.
     *
     * @return true when environment variable
     *         {@value TestUtil#SECHUB_TEST_TRACEMODE} is set to `true` otherwise
     *         false
     */
    public static boolean isTraceEnabled() {
        return Boolean.parseBoolean(System.getenv(SECHUB_TEST_TRACEMODE));
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
     * `./build/sechub/tmp/${prefix}_tmp_${nanoTime}.${fileEnding}`. When
     * environment entry `{@value TestUtil#SECHUB_KEEP_TEMPFILES}` is set to `true`
     * those files will be kept when JVM exits. Otherwise, those files will be
     * deleted by JVM on shutdown phase normally.
     *
     * @param prefix     filename prefix
     * @param fileEnding filename ending
     * @return filePath
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
     * @param dirNectoryBaseName represents the base name used for the directory
     * @return
     * @throws IOException
     */
    public static Path createTempDirectoryInBuildFolder(String dirNectoryBaseName, FileAttribute<?>... attributes) throws IOException {
        Path tmpPath = ensureBuildTmpDirAsFile();

        Path tempFolder = tmpPath.toRealPath().resolve(dirNectoryBaseName + "tmp_" + System.nanoTime());
        if (Files.notExists(tempFolder)) {
            Files.createDirectories(tempFolder, attributes); // create all missing directories

            if (isDeletingTempFiles()) {
                currentTempFolders.add(tempFolder);
            }
        }

        return tempFolder;
    }

    public static Path createTempFileInBuildSubFolder(String subFolderName, String explicitFileName, FileAttribute<?>... attributes) throws IOException {
        Path parent = ensureBuildTmpDirAsFile();
        Path tempSubFolder = null;
        if (subFolderName != null) {
            tempSubFolder = parent.resolve(subFolderName);
            parent = tempSubFolder;
        }
        Path filePath = parent.resolve(explicitFileName);

        if (Files.exists(filePath)) {
            LOG.warn("Temporary file already exists and will be deleted:{}", filePath);
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot delete former temp file", e);
            }
        }
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath, attributes);

        if (isDeletingTempFiles()) {
            if (tempSubFolder != null) {
                currentTempFolders.add(tempSubFolder);
            } else {
                filePath.toFile().deleteOnExit(); // mark file as to auto delete at the end (means directly inside tmp folder)
            }
        }
        return filePath;
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
        return createTempFileInBuildSubFolder(null, explicitFileName, attributes);
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
        return SystemUtils.IS_OS_WINDOWS;
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

    public static void assertExceptionMessageContains(IllegalStateException exception, String expectedMessageToBeContained) {
        String message = exception.getMessage();
        if (!message.contains(expectedMessageToBeContained)) {
            assertEquals("Is not containing: " + expectedMessageToBeContained, message);
        }
    }

    public static void assertExceptionMessageStartsWith(IllegalStateException exception, String expectedMessageStart) {
        String message = exception.getMessage();
        if (!message.startsWith(expectedMessageStart)) {
            assertEquals("Is not starting with: " + expectedMessageStart, message);
        }
    }

    public static boolean hasAtLeastOneMethodWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotated = method.getAnnotationsByType(annotation);
            if (annotated == null || annotated.length == 0) {
                continue;
            } else {
                return true;
            }

        }
        return false;
    }

    public static String createInfoForList(List<?> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("List:\n");
        for (Object obj : data) {
            sb.append(obj);
            sb.append("\n");
        }
        return sb.toString();
    }

}
