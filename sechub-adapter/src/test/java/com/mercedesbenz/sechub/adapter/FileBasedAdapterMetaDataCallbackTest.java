// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.test.TestUtil;

class FileBasedAdapterMetaDataCallbackTest {

    private static File testFolder;
    private File testFile;
    private FileBasedAdapterMetaDataCallback callbackToTest;

    @BeforeAll
    static void beforeAll() throws IOException {
        testFolder = TestUtil.createTempDirectoryInBuildFolder("filestore").toFile();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        testFile = new File(testFolder, "filestore_" + System.nanoTime() + ".txt");

        callbackToTest = new FileBasedAdapterMetaDataCallback(testFile);

    }

    @Test
    void file_does_not_exist_getMetaDataOrNull_returns_null() throws Exception {
        /* execute */
        AdapterMetaData metaData = callbackToTest.getMetaDataOrNull();

        /* test */
        assertNull(metaData);

    }

    @Test
    void reading_an_empty_file_does_return_null() throws Exception {
        /* prepare */
        assertTrue(testFile.createNewFile());

        /* execute */
        AdapterMetaData metaData = callbackToTest.getMetaDataOrNull();

        /* test */
        assertNull(metaData);

    }

    @Test
    void reading_a_clean_json_does_return_empty_metadata_object() throws Exception {
        /* prepare */
        TestFileWriter writer = new TestFileWriter();
        writer.writeTextToFile(testFile, "{}", true);

        /* execute */
        AdapterMetaData metaData = callbackToTest.getMetaDataOrNull();

        /* test */
        assertNotNull(metaData);

    }

    @Test
    void file_does_not_exist_meta_data_can_be_persisted_and_read_again() throws Exception {
        /* check precondition */
        assertFalse(testFile.exists());

        /* prepare */
        AdapterMetaData createdMetaData = new AdapterMetaData();
        createdMetaData.setValue("my.boolean.key", true);
        createdMetaData.setValue("my.string.key", "hello world");

        /* execute 1 */
        callbackToTest.persist(createdMetaData);

        /* test 1 */
        assertTrue(testFile.exists());

        /* execute 2 */
        AdapterMetaData loadedMetaData = callbackToTest.getMetaDataOrNull();

        /* test 2 */
        assertNotNull(loadedMetaData);
        assertEquals("hello world", loadedMetaData.getValueAsStringOrNull("my.string.key"));
        assertEquals(true, loadedMetaData.getValueAsBoolean("my.boolean.key"));

    }

    @Test
    void multi_store_last_one_fetched() throws Exception {
        /* prepare */
        AdapterMetaData createdMetaData = new AdapterMetaData();
        createdMetaData.setValue("my.boolean.key", true);
        createdMetaData.setValue("my.string.key", "hello world");
        callbackToTest.persist(createdMetaData);
        createdMetaData.setValue("my.string.key", "hello world2");
        callbackToTest.persist(createdMetaData);
        createdMetaData.setValue("my.string.key", "hello world3");
        callbackToTest.persist(createdMetaData);

        /* execute 2 */
        AdapterMetaData loadedMetaData = callbackToTest.getMetaDataOrNull();

        /* test 2 */
        assertNotNull(loadedMetaData);
        assertEquals("hello world3", loadedMetaData.getValueAsStringOrNull("my.string.key"));
        assertEquals(true, loadedMetaData.getValueAsBoolean("my.boolean.key"));

    }

}
