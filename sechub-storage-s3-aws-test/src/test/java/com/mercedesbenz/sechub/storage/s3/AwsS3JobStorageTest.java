// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import static com.mercedesbenz.sechub.test.JUnitAssertionAddon.assertThrowsExceptionContainingMessage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.s3.AmazonS3;

class AwsS3JobStorageTest {
    private String bucketName;
    private AmazonS3 mockedClient;
    private String storagePath;
    private UUID jobUUID;
    private AwsS3JobStorage jobStorage;

    @BeforeEach
    void beforeEach() throws Exception {
        bucketName = "test";
        storagePath = "jobs";
        mockedClient = mock(AmazonS3.class);
        jobUUID = UUID.fromString("cbec759a-0cb4-11ed-8f8e-b3bb9e50cb79");
        jobStorage = new AwsS3JobStorage(mockedClient, bucketName, storagePath, jobUUID);
    }

    @Test
    void bucket_exists() throws IOException {
        /* prepare */
        String objectName = storagePath + "/" + jobUUID + "/" + bucketName;
        when(mockedClient.doesObjectExist(bucketName, objectName)).thenReturn(true);

        /* execute + test */
        assertTrue(jobStorage.isExisting(bucketName));
    }

    @Test
    void bucket_does_not_exists() throws IOException {
        /* prepare */
        String objectName = storagePath + "/" + jobUUID + "/" + bucketName;
        when(mockedClient.doesObjectExist(bucketName, objectName)).thenReturn(false);

        /* execute + test */
        assertFalse(jobStorage.isExisting(bucketName));
    }

    @Test
    void store_name_null() {
        /* prepare */
        String fileName = null;
        String data = "test";
        InputStream input = new ByteArrayInputStream(data.getBytes());
        long contentLength = data.length();

        /* execute + test */
        assertThrowsExceptionContainingMessage(NullPointerException.class, "name may not be null!", () -> jobStorage.store(fileName, input, contentLength));
    }

    @Test
    void store_input_stream_null() {
        /* prepare */
        String fileName = "test-file.txt";
        InputStream input = null;

        /* execute + test */
        assertThrowsExceptionContainingMessage(NullPointerException.class, "inputStream may not be null!", () -> jobStorage.store(fileName, input, 0));
    }

    @Test
    void store_content_length_negative() {
        /* prepare */
        String fileName = "test-file.txt";
        String data = "test";
        InputStream input = new ByteArrayInputStream(data.getBytes());
        long contentLength = -1;

        /* execute + test */
        assertThrowsExceptionContainingMessage(IllegalArgumentException.class, "Content length cannot be negative!",
                () -> jobStorage.store(fileName, input, contentLength));
    }
}