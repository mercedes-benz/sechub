// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockMultipartFile;

import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.PDSNotFoundException;
import com.daimler.sechub.pds.util.PDSFileChecksumSHA256Service;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class PDSFileUploadJobServiceTest {

    private static final String ACCEPTED_CHECKSUM = "checksum-accepted";
    private static final String NOT_ACCEPTED_CHECKSUM = "checksum-failing";

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    
    private PDSFileUploadJobService serviceToTest;
    private UUID jobUUID;
    private PDSFileChecksumSHA256Service checksumService;
    private Path tmpUploadPath;

    private PDSJobRepository repository;

    private PDSJob job;

    private PDSWorkspaceService workspaceService;

    @Before
    public void before() throws Exception {
        tmpUploadPath = Files.createTempDirectory("pds-upload");
        jobUUID = UUID.randomUUID();
        checksumService=mock(PDSFileChecksumSHA256Service.class);
        workspaceService=mock(PDSWorkspaceService.class);
        when(workspaceService.getUploadFolder(jobUUID)).thenReturn(new File(tmpUploadPath.toFile(),jobUUID.toString()));
        
        repository=mock(PDSJobRepository.class);
        job = new PDSJob();
        job.uUID=jobUUID;
        
        Optional<PDSJob> jobOption = Optional.of(job);
        when(repository.findById(jobUUID)).thenReturn(jobOption);
        
        serviceToTest = new PDSFileUploadJobService();
        serviceToTest.checksumService=checksumService;
        serviceToTest.workspaceService=workspaceService;
        serviceToTest.repository=repository;
        
        when(checksumService.hasCorrectChecksum(eq(ACCEPTED_CHECKSUM), any())).thenReturn(true);
        when(checksumService.hasCorrectChecksum(eq(NOT_ACCEPTED_CHECKSUM), any())).thenReturn(false);
    }


    @Test
    public void upload_all_correct_but_job_not_found_throws_pds_not_found_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "1234567890123456789012345678901234567890";
        assertEquals(40,fileName.length());// check precondition
        
        /* test */
        expected.expect(PDSNotFoundException.class);
        expected.expectMessage("Given job does not exist");

        /* execute */
        UUID notExistingJobUUID = UUID.randomUUID();
        serviceToTest.upload(notExistingJobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        
    }
    
    @Test
    public void upload_all_correct_job_found_but_in_state_ready_to_start_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "1234567890123456789012345678901234567890";
        assertEquals(40,fileName.length());// check precondition
        job.setState(PDSJobStatusState.READY_TO_START);
        
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("accepted is only:[CREATED]");

        /* execute */
        serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        
    }

    @Test
    public void upload_containing_filename_length_41_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "12345678901234567890123456789012345678901";
        assertEquals(41,fileName.length());// check precondition
        
        /* test */
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("40");

        /* execute */
        serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        
    }
    
    @Test
    public void upload_containing_filename_with_slash_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "123456789/123456789012345678901234567890";
        assertEquals(40,fileName.length());// check precondition
        
        /* test */
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("[a-zA-Z");

        /* execute */
        serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        
    }
    
    @Test
    public void upload_containing_filename_with_backslash_throws_illegal_argument_exception() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String fileName = "123456789\\123456789012345678901234567890";
        assertEquals(40,fileName.length());// check precondition
        
        /* test */
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("[a-zA-Z");

        /* execute */
        serviceToTest.upload(jobUUID, fileName, multiPart, ACCEPTED_CHECKSUM);
        
    }
    
    @Test
    public void upload_uploads_given_content_to_file_to_specified_path() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.zip";
        assertEquals(40,allowedNameWithMaxLength.length());
        
        /* execute */
        serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, ACCEPTED_CHECKSUM);
        
        /* test */
        assertFileUploaded(allowedNameWithMaxLength);
        
    }
    
    @Test
    public void upload_uploads_given_content_to_file_to_specified_path_fails_when_checksum_service_says_not_correct_checksum() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());
        String allowedNameWithMaxLength = "123456789-123456789_123456789.123456.zip";
        assertEquals(40,allowedNameWithMaxLength.length());
        
        /* test */
        expected.expect(PDSNotAcceptableException.class);
        expected.expectMessage("checksum");
        expected.expectMessage("failed");
        
        /* execute */
        serviceToTest.upload(jobUUID, allowedNameWithMaxLength, multiPart, NOT_ACCEPTED_CHECKSUM);
        
    }
    
    @Test
    public void upload_and_delete_jobdata_jobfolder_has_been_removed() {
        /* prepare */
        String result = "content data";
        MockMultipartFile multiPart = new MockMultipartFile("file", result.getBytes());

        serviceToTest.upload(jobUUID, "fileName1.zip", multiPart, ACCEPTED_CHECKSUM);
        
        /* check precondition */
        assertTrue(jobFolder().exists());
        
        /* execute */
        serviceToTest.deleteAllUploads(jobUUID);
        
        /* test */
        assertFalse(jobFolder().exists());
        
        
    }
    private void assertFileUploaded(String fileName) {
        File file= new File(jobFolder(),fileName);
        if (!file.exists()) {
            fail("Expected file does not exist: "+file.getAbsolutePath()+"\n"+ dumpExistingFiles());
        }
        
    }

    private File jobFolder() {
        File file= new File(tmpUploadPath.toFile(),jobUUID.toString());
        return file;
    }
    
    
    private StringBuilder dumpExistingFiles() {
        File file = jobFolder();
        StringBuilder sb = new StringBuilder();
        sb.append("Existing files inside:").append(file.getAbsolutePath()).append("\n");
        for (File child: file.listFiles()) {
            sb.append(child.getName());
            sb.append("\n");
        }
        return sb;
    }

}
