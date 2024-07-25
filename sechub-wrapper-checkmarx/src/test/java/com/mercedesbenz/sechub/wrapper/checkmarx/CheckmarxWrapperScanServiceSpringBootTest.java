// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.checkmarx.factory.CheckmarxWrapperPDSUserMessageSupportFactory;
import com.mercedesbenz.sechub.wrapper.checkmarx.factory.CheckmarxWrapperPojoFactory;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperScanContextFactory;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperScanService;

@SpringBootTest(classes = { CheckmarxWrapperScanContextFactory.class, CheckmarxWrapperScanService.class, CheckmarxWrapperPojoFactory.class,
        CheckmarxWrapperEnvironment.class, CheckmarxWrapperPDSUserMessageSupportFactory.class })
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
        AdapterExecutionResult executionResult = scanService.startScan();

        /* test */
        String productResult = executionResult.getProductResult();

        assertNotNull(productResult);
        assertTrue(productResult.startsWith("<?xml"));
        assertTrue(productResult.contains("Checkmarx"));

        assertEquals(0, executionResult.getProductMessages().size());

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
