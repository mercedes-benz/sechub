// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import static com.mercedesbenz.sechub.test.JUnitAssertionAddon.assertThrowsExceptionContainingMessage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.mercedesbenz.sechub.storage.s3.aws.AwsS3JobStorage;

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

    @Test
    void store_content_and_check_content_length() throws IOException {
        /* prepare */
        String fileName = "test-file.txt";
        String data = "test";
        InputStream input = new ByteArrayInputStream(data.getBytes());
        long contentLength = data.length();
        when(mockedClient.doesBucketExistV2(bucketName)).thenReturn(true);
        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);

        /* execute */
        jobStorage.store(fileName, input, contentLength);

        /* test */
        verify(mockedClient, times(1)).putObject(eq(bucketName), any(String.class), any(InputStream.class), metadataCaptor.capture());

        ObjectMetadata capturedMetadata = metadataCaptor.getValue();
        assertEquals(contentLength, capturedMetadata.getContentLength());
    }
}