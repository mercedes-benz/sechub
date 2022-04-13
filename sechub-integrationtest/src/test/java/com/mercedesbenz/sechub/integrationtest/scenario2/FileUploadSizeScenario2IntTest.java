// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestDataConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.NotAcceptable;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.mercedesbenz.sechub.sharedkernel.util.ChecksumSHA256Service;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class FileUploadSizeScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(240);

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private ChecksumSHA256Service checksumSHA256Service;

    /**
     * Generate binaries tar file and violate file size limit
     *
     * @throws IOException
     */
    @Test
    public void when_binaries_tarfile_exceeds_configured_max_bin_file_size_a_BAD_REQUEST_is_returned() throws IOException {
        /* @formatter:off */
        handleBinariesCodeUpload(CONFIGURED_INTEGRATION_TEST_MAX_BINARIES_UPLOAD_IN_BYTES+1, true);
    }

    /**
     * Generate binaries tar file - with accepted size (so do not violate)
     *
     * @throws IOException
     */
    @Test
    public void when_binaries_tarfile_exceeds_NOT_max_bin_file_size_no_exception_is_thrown() throws IOException {
        /* @formatter:off */
        handleBinariesCodeUpload(CONFIGURED_INTEGRATION_TEST_MAX_BINARIES_UPLOAD_IN_BYTES-1,false);
    }

    /**
     * Generate binaries tar file - with accepted size (so do not violate)
     *
     * @throws IOException
     */
    @Test
    public void when_binaries_tarfile_is_only_one_kilobyte_no_exception_thrown() throws IOException {
        /* @formatter:off */
        handleBinariesCodeUpload(1024,false);
    }

    /**
     * Generate source ZIP file and violate file size limit
     *
     * @throws IOException
     */
    @Test
    public void when_sourcecode_zipfile_exceeds_configured_max_source_zip_file_size_a_NOT_ACCEPTABLE_is_returned() throws IOException {
        /* @formatter:off */
		handleSourceCodeUpload(CONFIGURED_INTEGRATION_TEST_MAX_GENERAL_UPLOAD_IN_BYTES,true);
	}

	/**
	 * Generate source ZIP file - with accepted size (so do not violate)
	 *
	 * @throws IOException
	 */
	@Test
	public void source_when_sourcecode_zipfile_exceeds_NOT_max_source_zip_file_size_no_exception_is_thrown() throws IOException {
		/* @formatter:off */
		handleSourceCodeUpload(CONFIGURED_INTEGRATION_TEST_MAX_GENERAL_UPLOAD_IN_BYTES,false);
	}


	/* ++++++++++++++++++++++++++++++++++++++++++++++++++++*/
    /* + ................Helpers......................... +*/
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++*/
	private void handleBinariesCodeUpload(int kiloBytes, boolean tooBig) throws FileNotFoundException, IOException {
        /* prepare */
        checksumSHA256Service = new ChecksumSHA256Service();
        as(SUPER_ADMIN).
            assignUserToProject(USER_1, PROJECT_1);

        UUID jobUUID = assertUser(USER_1).
                    doesExist().
                    isAssignedToProject(PROJECT_1).
                    canCreateWebScan(PROJECT_1);

        File fileToUpload = createTarFileContainingKilobytes(kiloBytes, tooBig);

        /* test */
        if (tooBig) {
            expected.expect(BadRequest.class);
            expected.expectMessage("Binaries upload maximum reached. Please reduce your upload file size.");
        }else {
            /* nothing - means expected no exception at all!*/
        }

        /* execute */
        try(InputStream inputStream = new FileInputStream(fileToUpload)){
            as(USER_1).
                uploadBinaries(PROJECT_1, jobUUID, fileToUpload, checksumSHA256Service.createChecksum(inputStream));
        }
        /* @formatter:on */
    }

    private void handleSourceCodeUpload(int kiloBytes, boolean tooBig) throws FileNotFoundException, IOException {
        /* prepare */
        checksumSHA256Service = new ChecksumSHA256Service();
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        UUID jobUUID = assertUser(USER_1).doesExist().isAssignedToProject(PROJECT_1).canCreateWebScan(PROJECT_1);

        File fileToUpload = createZipFileContainingKilobytes(kiloBytes, tooBig);

        /* test */
        if (tooBig) {
            expected.expect(NotAcceptable.class);
            expected.expectMessage("File upload maximum reached. Please reduce your upload file size.");
        } else {
            /* nothing - means expected no exception at all! */
        }

        /* execute */
        try (InputStream inputStream = new FileInputStream(fileToUpload)) {
            as(USER_1).upload(PROJECT_1, jobUUID, fileToUpload, checksumSHA256Service.createChecksum(inputStream));
        }
        /* @formatter:on */
    }

    /**
     * A little bit tricky: ZipFile content differs from file size. Also multipart
     * upload contains not only the file but meta information as well (e.g.
     * filename, sha256checksum,..)
     */
    private File createZipFileContainingKilobytes(int maximumBytes, boolean uploadShallBeTooLarge) throws FileNotFoundException, IOException {
        String tmpPath = "build/resources/bigFile";
        if (uploadShallBeTooLarge) {
            tmpPath += "-too-large";
        } else {
            tmpPath += "-accepted";
        }
        File file = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(), "sechub-integrationtest/" + tmpPath + ".zip");
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        file.getParentFile().mkdirs(); // ensure parent folder structure exists, avoid FileNotFoundException because of
                                       // parent missing

        int maximumUploadSizeInBytes = maximumBytes;
        int bytesToOrder = maximumUploadSizeInBytes;
        if (!uploadShallBeTooLarge) {
            bytesToOrder = bytesToOrder - (3 * 1024); // we reduce 3kb (includes zipfile overhead, filename on multipart and sha256
                                                      // checksum on upload)
        }
        byte[] content = new byte[bytesToOrder];
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));) {

            ZipEntry zipEntry = new ZipEntry("test.bin");
            // Set compression level to minimum to generate big zip file
            zipOutputStream.setLevel(0);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(content);
            zipOutputStream.flush();
        }
        if (uploadShallBeTooLarge && file.length() < maximumUploadSizeInBytes) {
            throw new IllegalStateException("Testcase corrupt: Wanted at least file size: " + maximumUploadSizeInBytes + " but was:" + file.length());
        }
        if (!uploadShallBeTooLarge && file.length() >= maximumUploadSizeInBytes) {
            throw new IllegalStateException(
                    "Testcase corrupt: Wanted a maximum file size: " + (maximumUploadSizeInBytes - (3 * 1024)) + " but was:" + file.length());
        }
        return file;
    }

    private File createTarFileContainingKilobytes(int maximumBytes, boolean uploadShallBeTooLarge) throws FileNotFoundException, IOException {
        String tmpPath = "build/resources/bigFile";
        if (uploadShallBeTooLarge) {
            tmpPath += "-too-large";
        } else {
            tmpPath += "-accepted";
        }
        File file = new File(IntegrationTestFileSupport.getTestfileSupport().getRootFolder(), "sechub-integrationtest/" + tmpPath + ".tar");
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        file.getParentFile().mkdirs(); // ensure parent folder structure exists, avoid FileNotFoundException because of
                                       // parent missing

        int maximumUploadSizeInBytes = maximumBytes;
        int bytesToOrder = maximumUploadSizeInBytes;

        byte[] content = new byte[bytesToOrder];
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(content); // we do not care what is inside...just write it
            fileOutputStream.flush();
        }
        if (uploadShallBeTooLarge && file.length() < maximumUploadSizeInBytes) {
            throw new IllegalStateException("Testcase corrupt: Wanted at least file size: " + maximumUploadSizeInBytes + " but was:" + file.length());
        }
        if (!uploadShallBeTooLarge && file.length() > maximumUploadSizeInBytes) {
            throw new IllegalStateException(
                    "Testcase corrupt: Wanted a maximum file size: " + (maximumUploadSizeInBytes - (3 * 1024)) + " but was:" + file.length());
        }
        return file;
    }
}
