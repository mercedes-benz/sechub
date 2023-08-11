// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.file.SimplePathVisitor;

/**
 * Simplifies some issues with eclipse versus gradle scan testing etc. (Gradle
 * does use always the root folder, Eclipse is using the current project as
 * relative root). Does also support convenient text file reading etc.
 *
 * Shall not be used directly, but instead have always a child implementation
 * which does the necessary setup etc.
 *
 * @author Albert Tregnaghi
 *
 */
@SechubTestComponent
public class TestFileSupport {

    private final File detectedGradleRoot;

    private String resourcePath;

    /**
     * Protected constructor. Create a child class and provide a public default
     * constructor, which calls this one with correct path
     *
     * @param projectTestResourcePath
     */
    protected TestFileSupport(String projectTestResourcePath) {
        if (projectTestResourcePath == null) {
            this.resourcePath = "";
        } else {
            if (projectTestResourcePath.endsWith("/")) {
                throw new IllegalArgumentException("Testcase corrupt, path may not end with / please change");
            }
            this.resourcePath = projectTestResourcePath;
        }

        File userDir = new File(System.getProperty("user.dir"));
        File gradleFolder = new File(userDir, "gradle");
        if (!gradleFolder.exists()) {
            // eclipse call from an eclipse project - so we got a src folder here
            File srcFolder = new File(userDir, "src");
            assertNotNull("no sourcefolder found!", srcFolder);

            File projectFolder = srcFolder.getParentFile();
            assertNotNull("no projectfolder found for source folder:" + srcFolder.getAbsolutePath(), projectFolder);

            File rootProjectFolder = projectFolder.getParentFile();
            assertNotNull("no root project folder found for project:" + projectFolder.getAbsolutePath(), rootProjectFolder);
            gradleFolder = new File(rootProjectFolder, "gradle");
        }
        if (gradleFolder.exists()) {
            detectedGradleRoot = gradleFolder.getParentFile();
        } else {
            throw new IllegalStateException("Testcase szenario corrupt, cannot determine gradle root folder!");
        }
    }

    public InputStream getInputStreamTo(String resourcePath) {
        File file = createFileFromResourcePath(resourcePath);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Create method does test file exists - and so this MUST exist here!", e);
        }
    }

    /**
     * @return detected gradle root folder
     */
    public File getRootFolder() {
        return detectedGradleRoot;
    }

    /**
     * Loads a test file from defined project test resource path + given path, will
     * use \n as line break
     *
     * @param pathFromRoot
     * @return text file
     * @throws IllegalArgumentException when file cannot be found (runtime exception
     *                                  to reduce bloating stuff)
     */
    public String loadTestFile(String path) {
        return loadTestFile(path, "\n");
    }

    /**
     * Loads a test file from defined project test resource path + given path - does
     * also asserts that file exists
     *
     * @param path - relative path, may not start with /
     * @return text file
     * @throws IllegalArgumentException when file cannot be found (runtime exception
     *                                  to reduce bloating stuff)
     */
    public String loadTestFile(String path, String lineBreak) {
        return loadTestFileFromRoot(createPathFromRoot(path), lineBreak);
    }

    public void writeTextFile(File file, String text) {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IllegalStateException("Was not able to delete former file:" + file);
            }
        }
        File parentFolder = file.getParentFile();
        if (!parentFolder.exists()) {
            if (!parentFolder.mkdirs()) {
                throw new IllegalStateException("Was not able to create parent folder:" + parentFolder);
            }

        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(text);
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to write text to file:" + file, e);
        }

    }

    private String createPathFromRoot(String path) {
        return resourcePath + "/" + path;
    }

    /**
     * Loads a test file from root path, will use \n as line break
     *
     * @param pathFromRoot
     * @return text file
     * @throws IllegalStateException when file cannot be found (runtime exception to
     *                               reduce bloating stuff)
     */
    public String loadTestFileFromRoot(String pathFromRoot) {
        return loadTestFileFromRoot(pathFromRoot, "\n");
    }

    /**
     * Loads a test file from root path - does also asserts that file exists
     *
     * @param pathFromRoot
     * @return text file
     * @throws IllegalStateException when file cannot be found (runtime exception to
     *                               reduce bloating stuff)
     */
    public String loadTestFileFromRoot(String pathFromRoot, String lineBreak) {
        if (lineBreak == null) {
            throw new IllegalArgumentException("Testcase corrupt: Line break may not be null!");
        }
        File file = createFileFromRoot(pathFromRoot);

        return loadTextFile(file, lineBreak);
    }

    /**
     * Load text file as string - use "\n" for line breaks
     *
     * @param file
     * @return string
     */
    public static String loadTextFile(File file) {
        return loadTextFile(file, "\n");
    }

    /**
     * Load text file as string
     *
     * @param file
     * @param lineBreak
     * @return string
     */
    public static String loadTextFile(File file, String lineBreak) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line = null;

            boolean firstEntry = true;
            while ((line = br.readLine()) != null) {
                if (!firstEntry) {
                    sb.append(lineBreak);
                }
                sb.append(line);
                firstEntry = false;// this prevents additional line break at end of file...
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Testcase corrupt: Cannot read test file " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Creates a file inside project - does also asserts that file exists
     *
     * @param pathFromRoot
     * @return file
     * @throws IllegalStateException when file cannot be found (runtime exception to
     *                               reduce bloating stuff)
     */
    public File createFileFromRoot(String pathFromRoot) {
        return assertFile(new File(detectedGradleRoot, pathFromRoot));
    }

    /**
     * Creates a file based resource path given at constructor- does also asserts
     * that file exists. So if you are using 'src/test/resources' in constructor
     * call as resource path you must be aware that you have no access to normal
     * resources... In this case use the {@link #createFileFromRoot(String)} method
     * instead
     *
     * @param pathInProject
     * @return file
     * @throws IllegalStateException when file cannot be found (runtime exception to
     *                               reduce bloating stuff)
     */
    public File createFileFromResourcePath(String pathInProject) {
        return assertFile(new File(createFileFromRoot(resourcePath), pathInProject));
    }

    private File assertFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file is null!");
        }
        if (!file.exists()) {
            throw new IllegalStateException("Testcase corrupt: Test file does not exist:" + file.getAbsolutePath());
        }
        return file;
    }

    public File createTempFile(String prefix, String ending) {
        try {
            Path path = TestUtil.createTempFileInBuildFolder(prefix, ending);
            return path.toFile();
        } catch (IOException e) {
            throw new IllegalStateException("cannot create temp file!", e);
        }
    }

    public static List<File> loadFilesAsFileList(File directory) throws IOException {
        List<File> files = new LinkedList<>();
        Files.walkFileTree(directory.toPath(), new SimplePathVisitor() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                files.add(file.toFile());
                return super.visitFile(file, attrs);
            }
        });
        return files;
    }
}
