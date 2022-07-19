package com.mercedesbenz.sechub.wrapper.checkmarx;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupService;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperContextFactory;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperScanService;

@SpringBootTest(classes = { CheckmarxWrapperContextFactory.class, CheckmarxWrapperScanService.class, CheckmarxWrapperPojoFactory.class,
        CheckmarxWrapperCLIEnvironment.class, MockedAdapterSetupService.class })
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class CheckmarxWrapperScanServiceSpringBootTest {

    @Value("${pds.job.extracted.sources.folder}")
    String buildFolderTestExtractionSourceFolderAsString;

    @Autowired
    CheckmarxWrapperScanService scanService;

    /*
     * Remark: the "recompressed" folder is created at the extracted source folder
     * parent to avoid having parts from build/test we define a build folder as the
     * extracted source folder and copy the resources at this location before the
     * test.
     */
    @BeforeEach
    void beforeEach() throws IOException {

        /* check file exists */
        String resourceFolderAsString = "./src/test/resources/testdata/workspace1/sources/extracted";
        File resourcesFolder = new File(resourceFolderAsString);
        assertFileDoesExist(resourcesFolder);

        /* cleanup extraction source folder */
        File buildFolderTestExtractionSourceFolder = new File(buildFolderTestExtractionSourceFolderAsString);
        FileUtils.deleteDirectory(buildFolderTestExtractionSourceFolder);
        assertFileDoesNotExist(buildFolderTestExtractionSourceFolder);

        /* copy resources */
        FileUtils.copyDirectory(resourcesFolder, buildFolderTestExtractionSourceFolder);

        assertFileDoesExist(buildFolderTestExtractionSourceFolder);

    }

    /**
     * In application-test.properties we have defined to use the mock adapter
     * variant and also other test relevant parts (team id and preset id mapping).
     * So we can execute the scan start completely here.
     *
     * @throws Exception
     */
    @Test
    void start_scan_with_checkmarx_mocked_adapter_is_possible_and_returns_not_null() throws Exception {

        /* execute */
        String result = scanService.startScan();

        /* test */
        assertNotNull(result);
        assertTrue(result.startsWith("<?xml"));
        assertTrue(result.contains("Checkmarx"));

    }

    private void assertFileDoesNotExist(File resourceFile) {
        internalAssertFileExists(resourceFile, false);
    }

    private void assertFileDoesExist(File resourceFile) {
        internalAssertFileExists(resourceFile, true);

    }

    private void internalAssertFileExists(File resourceFile, boolean shallExist) {

        if (shallExist && !resourceFile.exists()) {
            throw new IllegalStateException("Resource folder does not exist:" + resourceFile);
        }
        if (!shallExist && resourceFile.exists()) {
            throw new IllegalStateException("Resource folder does exist:" + resourceFile);
        }
    }
}
