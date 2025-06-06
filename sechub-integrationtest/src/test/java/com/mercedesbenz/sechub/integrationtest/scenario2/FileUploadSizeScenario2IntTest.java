// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestDataConstants.*;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;
import static com.mercedesbenz.sechub.test.JUnitAssertionAddon.*;
import static com.mercedesbenz.sechub.test.TestConstants.*;
import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.NotAcceptable;

import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestAPI;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestFileSupport;
import com.mercedesbenz.sechub.test.JUnitAssertionAddon.UnitTestExecutable;
import com.mercedesbenz.sechub.test.TestFileReader;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class FileUploadSizeScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(240);

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private CheckSumSupport checkSumSupport = new CheckSumSupport();

    private TestData testData;

    @Before
    public void before() {
        testData = new TestData();
    }

    @Test
    public void when_binaries_tarfile_exceeds_configured_max_bin_file_size_a_BAD_REQUEST_is_returned() throws Exception {

        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_FILE_UPLOAD_SIZE_IN_BYTES + 2221204;
        testData.tooBig = true;

        testData.expectedException = BadRequest.class;
        testData.expectedErrorMessagePart = "The file size in header field x-file-size exceeds the allowed upload size of 400 KB";

        /* execute + test */
        handleBinariesUpload(testData);
    }

    @Test
    public void when_binaries_tarfile_exceeds_NOT_max_bin_file_size_file_is_uploaded() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_FILE_UPLOAD_SIZE_IN_BYTES - 1;

        /* execute + test */
        handleBinariesUpload(testData);
    }

    @Test
    public void when_binaries_tarfile_exceeds_NOT_max_bin_file_size_but_invalid_user_checksum_NOT_ACCEPTABLE_is_returned() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_BINARIES_UPLOAD_IN_BYTES - 1;
        testData.userChecksum = "wrong-checksum";

        testData.expectedException = NotAcceptable.class;
        testData.expectedErrorMessagePart = "Sha256 checksum is not valid";

        /* execute + test */
        handleBinariesUpload(testData);
    }

    @Test
    public void when_source_zipfile_exceeds_NOT_max_bin_file_size_but_invalid_user_checksum_NOT_ACCEPTABLE_is_returned() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_FILE_UPLOAD_SIZE_IN_BYTES;
        testData.userChecksum = "wrong-checksum";

        testData.expectedException = NotAcceptable.class;
        testData.expectedErrorMessagePart = "Sha256 checksum is not valid";

        /* execute + test */
        handleSourcecodeUpload(testData);
    }

    @Test
    public void when_binaries_tarfile_exceeds_NOT_max_bin_file_size_but_differs_to_user_checksum_NOT_ACCEPTABLE_is_returned() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_BINARIES_UPLOAD_IN_BYTES - 1;
        // correct checksum:
        // 5f70bf18a086007016e948b04aed3b82103a36bea41755b6cddfaf10ace3c6ef
        testData.userChecksum = "5f70bf18a086007016e948b04aed3b82103a36bea41755b6cddfaf10ace3c6ee"; // last char changed so different

        testData.expectedException = BadRequest.class;
        testData.expectedErrorMessagePart = "checksum check failed";

        /* execute + test */
        handleBinariesUpload(testData);
    }

    @Test
    public void when_source_zipfile_exceeds_NOT_max_bin_file_size_but_differs_to_user_checksum_NOT_ACCEPTABLE_is_returned() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_FILE_UPLOAD_SIZE_IN_BYTES;
        // correct checksum:
        // cf414e31e73f986a9f3c8f76349a11d3a42d6880b3234258b5ff461c04a60b6f
        testData.userChecksum = "cf414e31e73f986a9f3c8f76349a11d3a42d6880b3234258b5ff461c04a60b6c"; // last char changed so different

        testData.expectedException = NotAcceptable.class;
        testData.expectedErrorMessagePart = "checksum check failed";

        /* execute + test */
        handleSourcecodeUpload(testData);
    }

    @Test
    public void when_binaries_tarfile_is_only_one_kilobyte_no_exception_thrown() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = 1024;

        /* execute + test */
        handleBinariesUpload(testData);
    }

    /**
     * The test setup allows for 300KB file upload size and 320KB request size.
     * With a file upload size > 300KB this should result in a file size exceeded exception.
     */
    @Test
    public void when_sourcecode_zipfile_exceeds_configured_max_source_zip_file_size_a_NOT_ACCEPTABLE_is_returned() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_FILE_UPLOAD_SIZE_IN_BYTES + 1;
        testData.tooBig = true;

        testData.expectedException = NotAcceptable.class;
        testData.expectedErrorMessagePart = "The file upload size must not exceed 300 KB";

        /* execute + test */
        handleSourcecodeUpload(testData);
    }

    /**
     * The test setup allows for 300KB file upload size and 320KB request size.
     * With a file upload size > 320KB this should result in a request size exceeded exception.
     * Note that the request size is always evaluated first.
     */
    @Test
    public void when_request_exceeds_configured_max_request_size_a_NOT_ACCEPTABLE_is_returned() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_REQUEST_SIZE_IN_BYTES + 1;
        testData.tooBig = true;

        testData.expectedException = NotAcceptable.class;
        testData.expectedErrorMessagePart = "The request size must not exceed 320 KB";

        /* execute + test */
        handleSourcecodeUpload(testData);
    }

    @Test
    public void when_sourcecode_zipfile_exceeds_NOT_max_source_zip_file_size_file_is_uploaded() throws Exception {
        /* prepare */
        testData.fileUploadSizeInBytes = CONFIGURED_INTEGRATION_TEST_MAX_FILE_UPLOAD_SIZE_IN_BYTES;

        /* execute + test */
        handleSourcecodeUpload(testData);
    }

    private class TestData {

        private Class<? extends Throwable> expectedException;
        private String expectedErrorMessagePart;

        private int fileUploadSizeInBytes;
        private boolean tooBig;
        private String userChecksum;
        public String fileNameAtServerSide;

        public boolean isExpectingAnException() {
            return expectedException != null;
        }
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void handleBinariesUpload(TestData data) throws Exception {
        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);
        /* @formatter:off */
        UUID jobUUID = assertUser(USER_1).
                    canCreateWebScan(PROJECT_1);

        File fileToUpload = createTarFileContainingKilobytes(data);
        /* @formatter:on */

        /* execute */
        UnitTestExecutable executable = new UnitTestExecutable() {

            @Override
            public void execute() throws Throwable {
                try (InputStream inputStream = new FileInputStream(fileToUpload)) {
                    String checksum = data.userChecksum;
                    if (checksum == null) {
                        checksum = checkSumSupport.createSha256Checksum(inputStream);
                    }
                    as(USER_1).uploadBinaries(PROJECT_1, jobUUID, fileToUpload, checksum);
                }
            }

        };
        /* test */
        data.fileNameAtServerSide = BINARIES_TAR;
        testErrorOrUploadDoneAsExpected(data, jobUUID, executable);

    }

    private void testErrorOrUploadDoneAsExpected(TestData data, UUID jobUUID, UnitTestExecutable executable) {
        if (data.isExpectingAnException()) {
            assertThrowsExceptionContainingMessage(data.expectedException, data.expectedErrorMessagePart, executable);
        } else {
            try {
                executable.execute();
            } catch (Throwable e) {
                throw new RuntimeException("Expected no exception but there was one", e);
            }

            /* test (additional when no exception ) */

            // check uploaded file
            File downloadedFile = TestAPI.getFileUploaded(PROJECT_1, jobUUID, data.fileNameAtServerSide);
            assertNotNull("Downloaded file may not be null!", downloadedFile);
            assertTrue("Downloaded file must exist!", downloadedFile.exists());

            long realFileSizeInBytes = downloadedFile.length();

            // check size information file available and as expected
            File downloadedFileSizeFile = TestAPI.getFileUploaded(PROJECT_1, jobUUID, data.fileNameAtServerSide + ".filesize");
            assertNotNull("Downloaded filesize file may not be null!", downloadedFileSizeFile);
            assertTrue("Downloaded filesize file must exist!", downloadedFileSizeFile.exists());

            String fetchedSizeAsString = TestFileReader.readTextFromFile(downloadedFileSizeFile);
            long fetchedSize = Long.parseLong(fetchedSizeAsString);

            assertEquals("Fetched file size not as expected for " + data.fileNameAtServerSide + " !", realFileSizeInBytes, fetchedSize);
        }

    }

    private void handleSourcecodeUpload(TestData data) throws FileNotFoundException, Exception {
        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        UUID jobUUID = assertUser(USER_1).canCreateWebScan(PROJECT_1);

        File fileToUpload = createZipFileContainingKilobytes(data.fileUploadSizeInBytes, data.tooBig);

        /* execute */
        UnitTestExecutable executable = new UnitTestExecutable() {

            @Override
            public void execute() throws Throwable {
                try (InputStream inputStream = new FileInputStream(fileToUpload)) {
                    String checksum = data.userChecksum;
                    if (checksum == null) {
                        checksum = checkSumSupport.createSha256Checksum(inputStream);
                    }
                    as(USER_1).uploadSourcecode(PROJECT_1, jobUUID, fileToUpload, checksum);
                }
            }

        };

        /* test */
        data.fileNameAtServerSide = SOURCECODE_ZIP;
        testErrorOrUploadDoneAsExpected(data, jobUUID, executable);

    }

    /**
     * A little bit tricky: ZipFile content differs from file size. Also multipart
     * upload contains not only the file but meta information as well (e.g.
     * filename, sha256checksum,..)
     */
    private File createZipFileContainingKilobytes(int maximumBytes, boolean uploadShallBeTooLarge) throws FileNotFoundException, Exception {
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

    private File createTarFileContainingKilobytes(TestData data) throws Exception {
        String tmpPath = "build/resources/bigFile";
        if (data.tooBig) {
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

        int maximumUploadSizeInBytes = data.fileUploadSizeInBytes;
        int bytesToOrder = maximumUploadSizeInBytes;

        byte[] content = new byte[bytesToOrder];
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(content); // we do not care what is inside...just write it
            fileOutputStream.flush();
        }
        if (data.tooBig && file.length() < maximumUploadSizeInBytes) {
            throw new IllegalStateException("Testcase corrupt: Wanted at least file size: " + maximumUploadSizeInBytes + " but was:" + file.length());
        }
        if (!data.tooBig && file.length() > maximumUploadSizeInBytes) {
            throw new IllegalStateException(
                    "Testcase corrupt: Wanted a maximum file size: " + (maximumUploadSizeInBytes - (3 * 1024)) + " but was:" + file.length());
        }
        return file;
    }
}
