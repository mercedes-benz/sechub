// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.test.TestUtil;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;

class CheckmarxWrapperScanContextTest {

    private CheckmarxWrapperScanContext contextToTest;
    private CheckmarxWrapperEnvironment environment;
    private SecHubConfigurationModel configuration;
    private ArchiveSupport archiveSupport;
    private MockDataIdentifierFactory mockDataIdentifierFactory;
    private PDSUserMessageSupport messageSupport;

    private TestFileWriter writer = new TestFileWriter();

    @BeforeEach
    void beforeEach() {

        configuration = mock(SecHubConfigurationModel.class);
        environment = mock(CheckmarxWrapperEnvironment.class);
        archiveSupport = mock(ArchiveSupport.class);
        messageSupport = mock(PDSUserMessageSupport.class);

        mockDataIdentifierFactory = mock(MockDataIdentifierFactory.class);

        contextToTest = new CheckmarxWrapperScanContext();
        contextToTest.configuration = configuration;
        contextToTest.environment = environment;
        contextToTest.archiveSupport = archiveSupport;
        contextToTest.mockDataIdentifierFactory = mockDataIdentifierFactory;
        contextToTest.messageSupport = messageSupport;

    }

    @Test
    void context_uses_mockdata_identifier_factory_to_create_mockdata_identifier() {
        /* prepare */
        String mockDataIdentifier = "path1;path2";
        when(mockDataIdentifierFactory.createMockDataIdentifier(ScanType.CODE_SCAN, configuration)).thenReturn(mockDataIdentifier);

        /* execute */
        String result = contextToTest.createMockDataIdentifier();

        /* test */
        assertEquals(mockDataIdentifier, result);
    }

    @Test
    void created_source_input_stream_can_be_read_and_contains_data_of_recompressed_file_of_extracted_sources() throws Exception {
        /* prepare */
        Path testFolder = TestUtil.createTempDirectoryInBuildFolder("chmx-wrapper-input-stream");
        File extractedFolder = new File(testFolder.toFile(), "extracted");
        extractedFolder.mkdirs();
        writer.writeTextToFile(new File(extractedFolder, "at-least-one-file.txt"), "content", false);

        File expectedCompressFolder = new File(extractedFolder.getParentFile(), "recompressed");
        File expectedTargetFile = new File(expectedCompressFolder, CommonConstants.FILENAME_SOURCECODE_ZIP);

        UUID uuid = UUID.randomUUID();
        String singleLineInWrittenFile = uuid.toString();

        when(environment.getPdsJobExtractedSourceFolder()).thenReturn(extractedFolder.getAbsolutePath());

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                // we simulate that archive support writes a "ZIP" file - instead of doing
                // a real compression the mock writes a single line into the output file.
                writer.writeTextToFile(expectedTargetFile, singleLineInWrittenFile, false);
                return null;
            }
        }).when(archiveSupport).compressFolder(eq(ArchiveType.ZIP), eq(extractedFolder), eq(expectedTargetFile));

        /* execute */
        InputStream stream = contextToTest.createSourceCodeZipFileInputStream();

        /* test */
        verify(archiveSupport).compressFolder(eq(ArchiveType.ZIP), eq(extractedFolder), eq(expectedTargetFile));

        // now let us check that the input stream can be read and contains the text file
        // written by mocked archive support
        assertNotNull(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = reader.readLine();

        assertEquals(singleLineInWrittenFile, line);

        reader.close();

    }

}
