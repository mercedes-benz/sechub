// SPDX-License-Identifier: MIT
package com.daimler.sechub.storage.s3;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import com.adobe.testing.s3mock.junit4.S3MockRule;
import com.amazonaws.services.s3.AmazonS3;
import com.daimler.sechub.storage.s3.aws.AwsS3JobStorage;
import com.daimler.sechub.test.TestPortProvider;

public class AwsS3JobStorageTest {

    private static final String TEST_DATA = "TEST-DATA";

    @ClassRule
    public static final S3MockRule S3_MOCK_RULE = S3MockRule.builder().withHttpPort(TestPortProvider.DEFAULT_INSTANCE.getS3MockServerHttpPort())
            .withHttpsPort(TestPortProvider.DEFAULT_INSTANCE.getS3MockServerHttpsPort()).
//			silent().
            build();

    private static AmazonS3 amazonTestClient;

    @BeforeClass
    public static void beforeClass() throws Exception {
        amazonTestClient = S3_MOCK_RULE.createS3Client();
    }

    @Test
    public void a_new_storage_does_not_create_a_new_bucket() throws Exception {

        /* execute */
        new AwsS3JobStorage(amazonTestClient, "bucket1", "jobstorage/projectName", UUID.randomUUID());

        /* test */
        assertTrue(amazonTestClient.listBuckets().isEmpty()); // also no bucket2 from former test execution...

    }

    @Test
    public void after_store_the_inputstream_is_closed() throws Exception {

        /* prepare */
        AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", UUID.randomUUID());

        Path tmpFile = Files.createTempFile("storage_test", ".txt");

        /* execute */
        InputStream inputStream = new FileInputStream(tmpFile.toFile());
        InputStream inputStreamSpy = Mockito.spy(inputStream);
        storage.store("testB", inputStreamSpy);

        /* test */
        Mockito.verify(inputStreamSpy, Mockito.atLeast(1)).close(); // stream must be closed

    }

    @Test
    public void store_stores_textfile_correct_and_can_be_fetched() throws Exception {

        /* prepare */
        UUID jobjUUID = UUID.randomUUID();
        String testContent = "line1\nline2";
        AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", jobjUUID);

        Path tmpFile = Files.createTempFile("storage_test", ".txt");
        BufferedWriter bw = Files.newBufferedWriter(tmpFile);

        bw.write(testContent);
        bw.close();

        /* execute */
        storage.store("testA", new FileInputStream(tmpFile.toFile()));

        /* test */
        String objectName = "jobstorage/projectName/" + jobjUUID + "/testA";
        assertTrue("Object must exist after storage", amazonTestClient.doesObjectExist("bucket2", objectName)); // test location is as expected

        AwsS3JobStorage storage2 = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", jobjUUID);
        InputStream loadedStream = storage2.fetch("testA");
        StringWriter writer = new StringWriter();
        IOUtils.copy(loadedStream, writer, Charset.defaultCharset());

        String loaded = writer.toString();

        assertEquals(testContent, loaded); // test content can be fetched

    }

    @Test
    public void stored_object_is_deleted_by_deleteall() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        AwsS3JobStorage storage = storeTestData(jobUUID);

        String objectName = "jobstorage/projectName/" + jobUUID + "/testC";
        assertTrue("Precondition not fullfilled, jobstorage not found", amazonTestClient.doesObjectExist("bucket2", objectName));

        /* execute */
        storage.deleteAll();

        /* test */
        assertFalse(amazonTestClient.doesObjectExist("bucket2", objectName));

    }

    @Test
    public void two_jobstorages_with_one_stored_object_one_storage_is_deleted_by_deleteall_other_still_exists() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        UUID jobUUID2 = UUID.randomUUID();

        AwsS3JobStorage storage = storeTestData(jobUUID);
        storeTestData(jobUUID2);

        String objectName = "jobstorage/projectName/" + jobUUID + "/testC";
        String objectName2 = "jobstorage/projectName/" + jobUUID2 + "/testC";

        assertTrue("storage object1 not found", amazonTestClient.doesObjectExist("bucket2", objectName));
        assertTrue("storage object1 not found", amazonTestClient.doesObjectExist("bucket2", objectName2));

        /* execute */
        storage.deleteAll();

        /* test */
        assertFalse(amazonTestClient.doesObjectExist("bucket2", objectName));
        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName2)); // still exists

    }

    @Test
    public void two_jobstorages_inside_different_jobs_are_fetachable() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        UUID jobUUID2 = UUID.randomUUID();

        AwsS3JobStorage storage1 = new AwsS3JobStorage(amazonTestClient, "bucket-1", "pds/test1", jobUUID);
        AwsS3JobStorage storage2 = new AwsS3JobStorage(amazonTestClient, "bucket-1", "pds/test1", jobUUID2);
        storeCreatedTestDataFile("file1.txt", storage1);
        storeCreatedTestDataFile("file2.txt", storage2);

        /* execute */
        InputStream fetched1 = storage1.fetch("file1.txt");
        InputStream fetched2 = storage2.fetch("file2.txt");

        /* test */
        assertNotNull(fetched1);
        assertNotNull(fetched2);

    }

    @Test
    public void job_storage_storing_alpha_and_beta__listNames__call_returns_alpha_and_beta() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        AwsS3JobStorage storage = storeTestData(jobUUID, "bucket2", "test/data/1", "alpha.txt");
        storeTestData(jobUUID, "bucket2", "test/data/1", "beta.txt");

        String objectName1 = "test/data/1/" + jobUUID + "/alpha.txt";
        String objectName2 = "test/data/1/" + jobUUID + "/beta.txt";

        assertTrue("storage object1 not found", amazonTestClient.doesObjectExist("bucket2", objectName1));
        assertTrue("storage object2 not found", amazonTestClient.doesObjectExist("bucket2", objectName2));

        /* execute */
        Set<String> result = storage.listNames();

        /* test */
        assertEquals(2, result.size());
        assertTrue(result.contains("alpha.txt"));
        assertTrue(result.contains("beta.txt"));

    }

    @Test
    public void job_storage_storing_alpha__alpha_is_listed_and_can_be_fetched() throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        String name = "alpha.txt";
        AwsS3JobStorage storage = storeTestData(jobUUID, "bucket2", "test/data/1", name);

        /* check precondition - listed as name */
        Set<String> result = storage.listNames();
        assertTrue(result.contains(name));

        /* execute again */
        InputStream fetched = storage.fetch(name);

        /* test */
        InputStreamReader reader = new InputStreamReader(fetched);
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        assertEquals(TEST_DATA, line);

    }

    private AwsS3JobStorage storeTestData(UUID jobUUID) throws IOException, FileNotFoundException {
        return storeTestData(jobUUID, "bucket2", "jobstorage/projectName", "testC");
    }

    private AwsS3JobStorage storeTestData(UUID jobUUID, String bucket, String storagePath, String filename) throws IOException, FileNotFoundException {
        AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, bucket, storagePath, jobUUID);

        return storeCreatedTestDataFile(filename, storage);
    }

    private AwsS3JobStorage storeCreatedTestDataFile(String name, AwsS3JobStorage storage) throws IOException, FileNotFoundException {
        Path tmpFile = Files.createTempFile("storage_test", ".txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile.toFile()))) {
            bw.write(TEST_DATA);
        }

        /* execute */
        InputStream inputStream = new FileInputStream(tmpFile.toFile());
        InputStream inputStreamSpy = Mockito.spy(inputStream);
        storage.store(name, inputStreamSpy);
        return storage;
    }

}
