package com.mercedesbenz.sechub.commons.archive;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.test.TestUtil;

class DirectoryAndFileSupportTest {

    private DirectoryAndFileSupport supportToTest;

    private TestFileWriter testFileWriter = new TestFileWriter();
    private File gitFolder;
    private File gitIgnoreInGitFolder;
    private File gitAttributesInGitFolder;
    private File testDataInGit;
    private File subFolder1;
    private File testDataInSubFolder1;
    private File gitAttributesInSubFolder1;
    private File gitIgnoreInSubFolder1;
    private File git2InsideSubFolder1;
    private File testDataInGit2;
    private File subFolder2InSubFolder1;
    private File testDataInSubFolder2;
    private File parentFolder;

    @BeforeEach
    void beforeEach() {
        supportToTest = new DirectoryAndFileSupport();
    }

    @Test
    void testfile_structure_removing_git_folder_git_attributes_and_git_ignore_files_recursive() throws Exception {
        /* prepare */
        initTestFileStructure();

        /* execute */
        supportToTest.cleanDirectories(parentFolder, (file) -> {
            if (file.isDirectory()) {
                return file.getName().equals(".git");
            }
            return file.getName().equals(".gitattributes") || file.getName().equals(".gitignore");
        });

        /* test */
        assertFalse(gitIgnoreInGitFolder.exists());
        assertFalse(gitAttributesInGitFolder.exists());
        assertFalse(testDataInGit.exists());
        assertTrue(testDataInSubFolder1.exists());
        assertFalse(gitAttributesInSubFolder1.exists());
        assertFalse(gitIgnoreInSubFolder1.exists());
        assertFalse(gitFolder.exists());
        assertFalse(git2InsideSubFolder1.exists());
        assertFalse(testDataInGit2.exists());
        assertTrue(testDataInSubFolder2.exists());

    }

    @Test
    void testfile_structure_removing_git_attributes_and_git_ignore_files_recursive() throws Exception {
        /* prepare */
        initTestFileStructure();

        /* execute */
        supportToTest.cleanDirectories(parentFolder, (file) -> {
            if (file.isFile()) {
                return file.getName().equals(".gitattributes") || file.getName().equals(".gitignore");
            }
            return false;
        });

        /* test */
        assertFalse(gitIgnoreInGitFolder.exists());
        assertFalse(gitAttributesInGitFolder.exists());
        assertTrue(testDataInGit.exists());
        assertTrue(testDataInSubFolder1.exists());
        assertFalse(gitAttributesInSubFolder1.exists());
        assertFalse(gitIgnoreInSubFolder1.exists());
        assertTrue(gitFolder.exists());
        assertTrue(git2InsideSubFolder1.exists());
        assertTrue(testDataInGit2.exists());
        assertTrue(testDataInSubFolder2.exists());

    }

    @Test
    void testfile_structure_removing_git_folder_only_recursive() throws Exception {
        /* prepare */
        initTestFileStructure();

        /* execute */
        supportToTest.cleanDirectories(parentFolder, (file) -> {
            if (file.isDirectory()) {
                return file.getName().equals(".git");
            }
            return false;
        });

        /* test */
        assertFalse(gitIgnoreInGitFolder.exists());
        assertFalse(gitAttributesInGitFolder.exists());
        assertFalse(testDataInGit.exists());
        assertTrue(testDataInSubFolder1.exists());
        assertTrue(gitAttributesInSubFolder1.exists());
        assertTrue(gitIgnoreInSubFolder1.exists());
        assertFalse(gitFolder.exists());
        assertFalse(git2InsideSubFolder1.exists());
        assertFalse(testDataInGit2.exists());
        assertTrue(testDataInSubFolder2.exists());

    }

    @Test
    void testfile_structure_nothing_removed_because_all_not_accepted_recursive() throws Exception {
        /* prepare */
        initTestFileStructure();

        /* execute */
        supportToTest.cleanDirectories(parentFolder, (file) -> {
            return false;
        });

        assertAllTestFileStructureFiles(true);

    }

    @Test
    void testfile_structure_all_removed_because_all_accepted_recursive() throws Exception {
        /* prepare */
        initTestFileStructure();

        /* execute */
        supportToTest.cleanDirectories(parentFolder, (file) -> {
            return true;
        });

        assertAllTestFileStructureFiles(false);

    }

    /**
     * Creates following file structure:
     *
     * <pre>
     *  /.git/.gitignore
     *  /.git/.gitattributes
     *  /.git/testdata-inside-git-folder.txt
     *  /subfolder1/testdata.txt
     *  /subfolder1/.gitattributes
     *  /subfolder1/.gitignore
     *  /subfolder1/.git/testdata-git2.txt
     *  /subfolder1/subfolder2/testdata-in-subfolder2
     * </pre>
     *
     * @throws IOException
     */
    void initTestFileStructure() throws IOException {
        parentFolder = TestUtil.createTempDirectoryInBuildFolder("directory-and-file-support").toFile();

        gitFolder = new File(parentFolder, ".git");
        gitIgnoreInGitFolder = new File(gitFolder, ".gitignore");
        gitAttributesInGitFolder = new File(gitFolder, ".gitattributes");
        testDataInGit = new File(gitFolder, "testdata-inside-git-folder.txt");

        testFileWriter.write(gitIgnoreInGitFolder, "testdata-gitignore-in-gitfolder");
        testFileWriter.write(gitAttributesInGitFolder, "testdata-gitattributes-in-gitfolder");
        testFileWriter.write(testDataInGit, "testdata-in-git-folder");

        subFolder1 = new File(parentFolder, "subfolder1");

        testDataInSubFolder1 = new File(subFolder1, "testdata.txt");
        gitAttributesInSubFolder1 = new File(subFolder1, ".gitattributes");
        gitIgnoreInSubFolder1 = new File(subFolder1, ".gitignore");

        testFileWriter.write(testDataInSubFolder1, "testdata-subfolder1");
        testFileWriter.write(gitAttributesInSubFolder1, "testdata-gitattributes-in-gitfolder");
        testFileWriter.write(gitIgnoreInSubFolder1, "testdata-in-git-folder");

        git2InsideSubFolder1 = new File(subFolder1, ".git");
        testDataInGit2 = new File(git2InsideSubFolder1, "testdata-git2.txt");
        testFileWriter.write(testDataInGit2, "testdata-in-git-folder2");

        subFolder2InSubFolder1 = new File(subFolder1, "subfolder2");
        testDataInSubFolder2 = new File(subFolder2InSubFolder1, "testdata-in-subfolder2");
        testFileWriter.write(testDataInSubFolder2, "testdata-in-subfolder2");

        /* check precondition */
        assertAllTestFileStructureFiles(true);

    }

    private void assertAllTestFileStructureFiles(boolean mustExist) {
        assertEquals(mustExist, gitFolder.exists());
        assertEquals(mustExist, gitIgnoreInGitFolder.exists());
        assertEquals(mustExist, gitAttributesInGitFolder.exists());
        assertEquals(mustExist, testDataInGit.exists());
        assertEquals(mustExist, testDataInSubFolder1.exists());
        assertEquals(mustExist, gitAttributesInSubFolder1.exists());
        assertEquals(mustExist, gitIgnoreInSubFolder1.exists());
        assertEquals(mustExist, git2InsideSubFolder1.exists());
        assertEquals(mustExist, testDataInGit2.exists());
        assertEquals(mustExist, testDataInSubFolder2.exists());
    }

}
