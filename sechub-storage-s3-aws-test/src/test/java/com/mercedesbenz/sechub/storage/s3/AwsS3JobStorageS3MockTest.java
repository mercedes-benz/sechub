// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.mercedesbenz.sechub.test.TestUtil;

@ExtendWith(S3MockExtension.class)
public class AwsS3JobStorageS3MockTest {

    private static final String NEVER_CREATED_BUCKET_ID = "bucket4711";

    private static final String TEST_DATA = "TEST-DATA";

    @Test
    public void a_new_storage_does_not_create_a_new_bucket(final AmazonS3 amazonTestClient) throws Exception {

        /* execute */
        new AwsS3JobStorage(amazonTestClient, NEVER_CREATED_BUCKET_ID, "jobstorage/projectName", UUID.randomUUID());

        /* test */
        List<Bucket> buckets = amazonTestClient.listBuckets();
        for (Bucket bucket : buckets) {
            String bucketName = bucket.getName();
            if (bucketName.equalsIgnoreCase(NEVER_CREATED_BUCKET_ID)) {
                fail("found bucket:" + bucketName);
            }
        }

    }

    @Test
    public void after_store_the_inputstream_is_closed(final AmazonS3 amazonTestClient) throws Exception {

        /* prepare */
        AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", UUID.randomUUID());

        Path tmpFilePath = TestUtil.createTempFileInBuildFolder("storage_test", "txt");
        File tmpFile = tmpFilePath.toFile();
        long tmpFileSize = tmpFile.length();

        /* execute */
        InputStream inputStream = new FileInputStream(tmpFile);
        InputStream inputStreamSpy = Mockito.spy(inputStream);
        storage.store("testB", inputStreamSpy, tmpFileSize);

        /* test */
        Mockito.verify(inputStreamSpy, Mockito.atLeast(1)).close(); // stream must be closed

    }

    @Test
    public void store_stores_textfile_correct_and_can_be_fetched(final AmazonS3 amazonTestClient) throws Exception {

        /* prepare */
        UUID jobjUUID = UUID.randomUUID();
        String testContent = "line1\nline2";
        AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", jobjUUID);

        Path tmpFilePath = TestUtil.createTempFileInBuildFolder("storage_test", "txt");
        File tmpFile = tmpFilePath.toFile();

        BufferedWriter bw = Files.newBufferedWriter(tmpFilePath);

        bw.write(testContent);
        bw.close();

        // the content length of the file after writing test data into it
        long contentLength = tmpFile.length();

        /* execute */
        storage.store("testA", new FileInputStream(tmpFile), contentLength);

        /* test */
        String objectName = "jobstorage/projectName/" + jobjUUID + "/testA";
        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName), "Object must exist after storage"); // test location is as expected

        AwsS3JobStorage storage2 = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", jobjUUID);
        InputStream loadedStream = storage2.fetch("testA");
        StringWriter writer = new StringWriter();
        IOUtils.copy(loadedStream, writer, Charset.defaultCharset());

        String loaded = writer.toString();

        assertEquals(testContent, loaded); // test content can be fetched

    }

    @Test
    public void stored_object_is_deleted_by_deleteall(final AmazonS3 amazonTestClient) throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        AwsS3JobStorage storage = storeTestData(amazonTestClient, jobUUID);

        String objectName = "jobstorage/projectName/" + jobUUID + "/testC";
        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName), "Precondition not fullfilled, jobstorage not found");

        /* execute */
        storage.deleteAll();

        /* test */
        assertFalse(amazonTestClient.doesObjectExist("bucket2", objectName));

    }

    @Test
    public void two_jobstorages_with_one_stored_object_one_storage_is_deleted_by_deleteall_other_still_exists(final AmazonS3 amazonTestClient)
            throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        UUID jobUUID2 = UUID.randomUUID();

        AwsS3JobStorage storage = storeTestData(amazonTestClient, jobUUID);
        storeTestData(amazonTestClient, jobUUID2);

        String objectName = "jobstorage/projectName/" + jobUUID + "/testC";
        String objectName2 = "jobstorage/projectName/" + jobUUID2 + "/testC";

        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName), "storage object1 not found");
        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName2), "storage object1 not found");

        /* execute */
        storage.deleteAll();

        /* test */
        assertFalse(amazonTestClient.doesObjectExist("bucket2", objectName));
        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName2)); // still exists

    }

    @Test
    public void two_jobstorages_inside_different_jobs_are_fetachable(final AmazonS3 amazonTestClient) throws Exception {
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
    public void job_storage_storing_alpha_and_beta__listNames__call_returns_alpha_and_beta(final AmazonS3 amazonTestClient) throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        AwsS3JobStorage storage = storeTestData(amazonTestClient, jobUUID, "bucket2", "test/data/a1", "alpha.txt");
        storeCreatedTestDataFile("beta.txt", storage);

        String objectName1 = "test/data/a1/" + jobUUID + "/alpha.txt";
        String objectName2 = "test/data/a1/" + jobUUID + "/beta.txt";

        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName1), "storage object1 not found");
        assertTrue(amazonTestClient.doesObjectExist("bucket2", objectName2), "storage object2 not found");

        /* execute */
        Set<String> result = storage.listNames();

        /* test */
        assertEquals(2, result.size());
        assertTrue(result.contains("alpha.txt"));
        assertTrue(result.contains("beta.txt"));

    }

    @Test
    public void storage1_storeds_alpha_storage2_stores_beta__listNames__for_storage1_returns_only_alpha_not_beta_and_versa(final AmazonS3 amazonTestClient)
            throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        AwsS3JobStorage storage = storeTestData(amazonTestClient, jobUUID, "bucket2", "test/data/b1", "alpha.txt");
        AwsS3JobStorage storage2 = storeTestData(amazonTestClient, jobUUID, "bucket2", "test/data/b2", "beta.txt");

        /* execute */
        Set<String> result = storage.listNames();
        Set<String> result2 = storage2.listNames();

        /* test */
        assertEquals(1, result.size());
        assertTrue(result.contains("alpha.txt"));
        assertFalse(result.contains("beta.txt"));

        assertEquals(1, result2.size());
        assertFalse(result2.contains("alpha.txt"));
        assertTrue(result2.contains("beta.txt"));

    }

    @Test
    public void storage1_storeds_job1_storage2_for_job2__listNames__for_storage1_returns_only_onefile__and_versa(final AmazonS3 amazonTestClient)
            throws Exception {
        /* prepare */
        AwsS3JobStorage storage1 = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", UUID.randomUUID());
        AwsS3JobStorage storage2 = new AwsS3JobStorage(amazonTestClient, "bucket2", "jobstorage/projectName", UUID.randomUUID());
        storeCreatedTestDataFile("file1", storage1);
        storeCreatedTestDataFile("file2", storage2);

        /* execute */
        Set<String> result = storage1.listNames();
        Set<String> result2 = storage1.listNames();

        /* test */
        assertEquals(1, result.size());
        assertTrue(result.contains("file1"));
        assertFalse(result.contains("file2"));

        assertEquals(1, result2.size());
        assertFalse(result2.contains("file2"));
        assertTrue(result2.contains("file1"));

    }

    @Test
    public void storage_but_nothing_uploaded_returns_empty_list(final AmazonS3 amazonTestClient) throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, "bucket2", "test/data/c1", jobUUID);

        /* execute */
        Set<String> result = storage.listNames();

        /* test */
        assertEquals(0, result.size());

    }

    @Test
    public void job_storage_storing_alpha__alpha_is_listed_and_can_be_fetched(final AmazonS3 amazonTestClient) throws Exception {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        String name = "alpha.txt";
        AwsS3JobStorage storage = storeTestData(amazonTestClient, jobUUID, "bucket2", "test/data/d1", name);

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

    private AwsS3JobStorage storeTestData(final AmazonS3 amazonTestClient, UUID jobUUID) throws IOException, FileNotFoundException {
        return storeTestData(amazonTestClient, jobUUID, "bucket2", "jobstorage/projectName", "testC");
    }

    private AwsS3JobStorage storeTestData(final AmazonS3 amazonTestClient, UUID jobUUID, String bucket, String storagePath, String filename)
            throws IOException, FileNotFoundException {
        AwsS3JobStorage storage = new AwsS3JobStorage(amazonTestClient, bucket, storagePath, jobUUID);

        return storeCreatedTestDataFile(filename, storage);
    }

    private AwsS3JobStorage storeCreatedTestDataFile(String name, AwsS3JobStorage storage) throws IOException, FileNotFoundException {
        /* prepare */
        Path tmpFilePath = TestUtil.createTempFileInBuildFolder("storage_test", "txt");
        File tmpFile = tmpFilePath.toFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile))) {
            bw.write(TEST_DATA);
        }

        // the file size after writing test data into it
        long tmpFileSize = tmpFile.length();

        /* execute */
        InputStream inputStream = new FileInputStream(tmpFile);
        InputStream inputStreamSpy = Mockito.spy(inputStream);
        storage.store(name, inputStreamSpy, tmpFileSize);
        return storage;
    }

}
