package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AutoCleanupGitFoldersTest {

    private AutoCleanupGitFoldersFilter filterToTest;

    @BeforeEach
    void beforeEach() {
        filterToTest = AutoCleanupGitFoldersFilter.INSTANCE;
    }

    @ParameterizedTest
    @ValueSource(strings = { ".gitignore", ".gitattributes", ".git", "other.md" })
    @EmptySource
    @NullSource
    void file_is_not_accepted(String fileName) {
        /* prepare */
        File file = simulateFile(fileName);

        /* execute */
        boolean result = filterToTest.accept(file);

        /* test */
        assertFalse(result);

    }

    @ParameterizedTest
    @ValueSource(strings = { ".git" })
    void directory_is_accepted(String fileName) {
        /* prepare */
        File file = simulateDirectory(fileName);

        /* execute */
        boolean result = filterToTest.accept(file);

        /* test */
        assertTrue(result);

    }

    @ParameterizedTest
    @ValueSource(strings = { "git", "env", ".gitignore" })
    @EmptySource
    @NullSource
    void directory_is_not_accepted(String fileName) {
        /* prepare */
        File file = simulateDirectory(fileName);

        /* execute */
        boolean result = filterToTest.accept(file);

        /* test */
        assertFalse(result);

    }

    private File simulateDirectory(String fileName) {
        return internalSimulateFile(fileName, true);
    }

    private File simulateFile(String fileName) {
        return internalSimulateFile(fileName, false);
    }

    private File internalSimulateFile(String fileName, boolean directory) {
        File file = mock(File.class);
        when(file.getName()).thenReturn(fileName);
        when(file.isDirectory()).thenReturn(directory);
        return file;
    }

}
