// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.s3;

import static com.mercedesbenz.sechub.test.JUnitAssertionAddon.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

class AwsS3JobStorageTest {
    private String bucketName;
    private AmazonS3 mockedClient;
    private String storagePath;
    private UUID jobUUID;
    private AwsS3JobStorage jobStorage;
    private TransferManager transferManager;

    @BeforeEach
    void beforeEach() throws Exception {
        bucketName = "test";
        storagePath = "jobs";
        mockedClient = mock(AmazonS3.class);
        jobUUID = UUID.fromString("cbec759a-0cb4-11ed-8f8e-b3bb9e50cb79");

        /* mock transfer manager parts */
        TransferManagerFactory factory = mock(TransferManagerFactory.class);
        transferManager = mock(TransferManager.class);
        when(factory.createTransferManager(mockedClient)).thenReturn(transferManager);

        jobStorage = new AwsS3JobStorage(mockedClient, bucketName, storagePath, jobUUID, factory);
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
    void store_waits_for_upload_completion() throws Exception {
        /* prepare */
        String fileName = "test-file.txt";
        String data = "test";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
        long contentLength = data.length();

        Upload upload = mockUpload(fileName, inputStream);

        /* execute + test */
        jobStorage.store(fileName, inputStream, contentLength);

        /* test */
        verify(upload).waitForCompletion();

    }

    @Test
    void store_throws_io_exception_when_upload_throws_client_excepton_while_waiting_for_completion() throws Exception {
        /* prepare */
        String fileName = "test-file.txt";
        String data = "test";
        long contentLength = data.length();

        InputStream inputStream = new ByteArrayInputStream(data.getBytes());

        Upload upload = mockUpload(fileName, inputStream);

        AmazonClientException amazonClientException = new AmazonClientException("ups...");
        doThrow(amazonClientException).when(upload).waitForCompletion();

        /* execute + test */
        IOException exception = assertThrows(IOException.class, () -> jobStorage.store(fileName, inputStream, contentLength));

        /* test */
        assertEquals("Store of: " + fileName + " to S3 bucket: " + bucketName + " failed", exception.getMessage());
        assertEquals(exception.getCause(), amazonClientException);

    }

    private Upload mockUpload(String fileName, InputStream inputStream) {
        Upload upload = mock(Upload.class);
        when(transferManager.upload(eq(bucketName), eq(storagePath + "/" + jobUUID + "/" + fileName), same(inputStream), any(ObjectMetadata.class)))
                .thenReturn(upload);
        return upload;
    }
}