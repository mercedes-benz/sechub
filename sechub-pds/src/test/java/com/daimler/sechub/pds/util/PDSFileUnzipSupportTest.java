// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.pds.util.PDSFileUnzipSupport.UnzipResult;

class PDSFileUnzipSupportTest {

    private PDSFileUnzipSupport supportToTest;

    @BeforeEach
    void beforeEach() {
        supportToTest = new PDSFileUnzipSupport();
    }

    @Test
    void single_file_zip_can_be_extracted() throws Exception {
        /* prepare */
        File singleZipfile = resolveTestFile("zipfiles/single_file.zip");
        File targetFolder = Files.createTempDirectory("pds_sinlgezip_test").toFile();
        targetFolder.mkdirs();

        /* execute */
        UnzipResult result = supportToTest.unzipArchive(singleZipfile, targetFolder);

        /* test */
        assertContainsFiles(targetFolder, "hardcoded_password.go");
        assertEquals(1,result.getExtractedFilesCount());
        assertEquals(0,result.getCreatedFoldersCount());
    }

    @Test
    void two_files_zip_can_be_extracted() throws Exception {
        /* prepare */
        File twoFilesZipfile = resolveTestFile("zipfiles/two_files.zip");
        File targetFolder = Files.createTempDirectory("pds_twofileszip_test").toFile();
        targetFolder.mkdirs();

        /* execute */
        UnzipResult result = supportToTest.unzipArchive(twoFilesZipfile, targetFolder);

        /* test */
        assertContainsFiles(targetFolder, "hardcoded_password.go", "README.md");
        assertEquals(2,result.getExtractedFilesCount());
        assertEquals(0,result.getCreatedFoldersCount());
    }

    @Test
    void hierarchical_files_zip__contained_folders_and_files_can_be_extracted() throws Exception {
        /* prepare */
        File abcZipfile = resolveTestFile("zipfiles/hierarchical_files.zip");

        File targetFolder = Files.createTempDirectory("pds_abczip_test").toFile();
        targetFolder.mkdirs();

        /* execute */
        UnzipResult result = supportToTest.unzipArchive(abcZipfile, targetFolder);

        /* test */
        assertContainsFiles(targetFolder, "abc");

        File abcFolder = assertFolderExists(targetFolder, "abc");
        assertContainsFiles(abcFolder, "def", "hardcoded_password.go", "sql_injection.go");

        File defFolder = assertFolderExists(abcFolder, "def");
        assertContainsFiles(defFolder, "ghi", "README-def.md");

        File ghiFolder = assertFolderExists(defFolder, "ghi");
        assertContainsFiles(ghiFolder, "README-ghi.md");

        assertEquals(4,result.getExtractedFilesCount());
        assertEquals(3,result.getCreatedFoldersCount());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ............... Helpers .......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private File assertFolderExists(File folder, String subFolder) {
        File[] children = folder.listFiles();
        for (File child : children) {
            if (child.getName().equals(subFolder)) {
                if (!child.isDirectory()) {
                    fail("Subfolder:" + subFolder + " found but is not a directory: "+child);
                }
                return child;
            }
        }
        fail("Subfolder:" + subFolder + " not found inside directory: "+folder);
        throw new IllegalStateException("Should not be called");

    }

    private void assertContainsFiles(File folder, String... childNames) {
        File[] children = folder.listFiles();
        assertEquals(childNames.length, children.length);

        /* build temporary list with names */
        List<String> foundChildNames = new ArrayList<>();

        for (File child : children) {
            foundChildNames.add(child.getName());
        }
        
        /* test names contained */
        for (String childName : childNames) {
            if (!foundChildNames.contains(childName)) {
                fail("Child: " + childName + " not found found inside list, but: " + foundChildNames);
            }
        }

    }

    private File resolveTestFile(String relativePath) {
        File file = new File("./src/test/resources/" + relativePath);
        assertTrue(file.exists(), "File must exist:" + file);
        return file;
    }

}
