// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.sharevolume.spring;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SharedVolumeJobStorageTest {

    private Path rootLocation;

    @BeforeEach
    void beforeEach() throws Exception {
        rootLocation = Files.createTempDirectory("sechub-sharedvolume-test");
    }

    @Test
    void listNames_returns_alpha_and_beta_names_when_these_are_stored() throws Exception {
        /* prepare */
        UUID uuid = UUID.randomUUID();

        SharedVolumeJobStorage storage = storeTestData(uuid, "alpha.txt");
        storeTestData(uuid, "beta.txt");

        /* execute */
        Set<String> result = storage.listNames();

        /* test */
        assertEquals(2, result.size());
        assertTrue(result.contains("alpha.txt"));
        assertTrue(result.contains("beta.txt"));

    }

    private SharedVolumeJobStorage storeTestData(UUID jobUUID, String fileName) throws IOException, FileNotFoundException {
        SharedVolumeJobStorage storage = new SharedVolumeJobStorage(rootLocation, "test1", jobUUID);

        Path tmpFile = Files.createTempFile("storage_test", ".txt");

        /* execute */
        InputStream inputStream = new FileInputStream(tmpFile.toFile());
        InputStream inputStreamSpy = Mockito.spy(inputStream);
        storage.store(fileName, inputStreamSpy);
        return storage;
    }

}
