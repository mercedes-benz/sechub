package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.test.TestFileWriter;
import com.mercedesbenz.sechub.test.TestUtil;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;

class CheckmarxWrapperContextTest {

    private CheckmarxWrapperContext contextToTest;
    private CheckmarxWrapperCLIEnvironment environment;
    private SecHubConfigurationModel configuration;
    private ArchiveSupport archiveSupport;
    private CodeScanPathCollector codeScanPathCollector;

    TestFileWriter writer = new TestFileWriter();

    @BeforeEach
    void beforeEach() {

        configuration = mock(SecHubConfigurationModel.class);
        environment = mock(CheckmarxWrapperCLIEnvironment.class);
        archiveSupport = mock(ArchiveSupport.class);
        codeScanPathCollector = mock(CodeScanPathCollector.class);

        contextToTest = new CheckmarxWrapperContext();
        contextToTest.configuration = configuration;
        contextToTest.environment = environment;
        contextToTest.archiveSupport = archiveSupport;
        contextToTest.codeScanPathCollector = codeScanPathCollector;

    }

    @Test
    void folder_calculation_uses_contained_sechub_configuration_and_the_path_collector() {
        /* prepare */
        when(codeScanPathCollector.collectAllCodeScanPathes(configuration)).thenReturn(new LinkedHashSet<>(Arrays.asList("path1", "path2")));

        /* execute */
        Set<String> result = contextToTest.calculateCodeUploadFileSystemFolders();

        /* test */
        assertNotNull(result);
        Set<String> expected = new LinkedHashSet<>();
        expected.add("path1");
        expected.add("path2");

        assertEquals(expected, result);
    }

    @Test
    void created_source_input_stream_can_be_read_and_contains_data_of_recompressed_file_of_extracted_sources() throws Exception {
        /* prepare */
        Path testFolder = TestUtil.createTempDirectoryInBuildFolder("chmx-wrapper-input-stream");
        File extractedFolder = new File(testFolder.toFile(), "extracted");
        extractedFolder.mkdirs();

        File expectedCompressFolder = new File(extractedFolder.getParentFile(), "recompressed");
        File expectedTargetFile = new File(expectedCompressFolder, CommonConstants.FILENAME_SOURCECODE_ZIP);

        UUID uuid = UUID.randomUUID();
        String singleLineInWrittenFile = uuid.toString();

        when(environment.getPdsJobExtractedSourceFolder()).thenReturn(extractedFolder.getAbsolutePath());

        doAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                writer.save(expectedTargetFile, singleLineInWrittenFile, false);
                return "saved";
            }
        }).when(archiveSupport).compressFolder(eq(ArchiveType.ZIP), eq(extractedFolder), eq(expectedTargetFile));

        /* execute */
        InputStream stream = contextToTest.createSourceCodeZipFileInputStream();

        /* test */
        verify(archiveSupport).compressFolder(eq(ArchiveType.ZIP), eq(extractedFolder), eq(expectedTargetFile));

        // now let us check that the input stream can be read and contains the
        assertNotNull(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = reader.readLine();

        assertEquals(singleLineInWrittenFile, line);

        reader.close();

    }

}
