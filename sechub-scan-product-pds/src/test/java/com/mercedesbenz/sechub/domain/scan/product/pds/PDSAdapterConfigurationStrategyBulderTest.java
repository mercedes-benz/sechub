// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.pds.PDSAdapterConfigData;
import com.mercedesbenz.sechub.adapter.pds.PDSCodeScanConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

class PDSAdapterConfigurationStrategyBulderTest {

    private InputStream binariesInputStream;
    private InputStream sourceInputStream;

    private PDSExecutorConfigSuppport configSupport;
    private PDSStorageContentProvider contentProvider;

    private PDSInstallSetup installSetup;

    private ProductExecutorData productExecutorData;
    private SecHubExecutionContext context;
    private SecHubConfiguration configuration;
    private UUID jobUUID;

    @BeforeEach
    void beforeEach() {
        binariesInputStream = mock(InputStream.class);
        sourceInputStream = mock(InputStream.class);
        configSupport = mock(PDSExecutorConfigSuppport.class);
        contentProvider = mock(PDSStorageContentProvider.class);
        installSetup = mock(PDSInstallSetup.class);
        productExecutorData = mock(ProductExecutorData.class);
        context = mock(SecHubExecutionContext.class);
        configuration = new SecHubConfiguration();

        jobUUID = UUID.randomUUID();

        when(productExecutorData.getSechubExecutionContext()).thenReturn(context);
        when(context.getConfiguration()).thenReturn(configuration);
        when(configSupport.getProductBaseURL()).thenReturn("https://example.com");
        when(configSupport.getPDSProductIdentifier()).thenReturn("PDS_PRODUCT1");
        when(context.getSechubJobUUID()).thenReturn(jobUUID);
    }

    /* @formatter:off */
    @Test
    void builder_can_create_a_binaries_codescan_setup_by_using_providers() throws Exception {

        /* prepare*/
        when(contentProvider.isBinaryRequired()).thenReturn(true);
        when(contentProvider.getBinariesTarFileSizeOrNull()).thenReturn("08154711");
        when(contentProvider.getBinariesTarFileUploadChecksumOrNull()).thenReturn("checksum1");

        /* execute */
        PDSAdapterConfigurationStrategy strategy =  PDSAdapterConfigurationStrategy.builder().
                setBinariesTarFileInputStreamOrNull(binariesInputStream).
                setConfigSupport(configSupport).
                setContentProvider(contentProvider).
                setInstallSetup(installSetup).
                setProductExecutorData(productExecutorData).
                setScanType(ScanType.CODE_SCAN).
                build();


        PDSCodeScanConfigImpl result = PDSCodeScanConfigImpl.builder().
                configure(strategy). // this is the execution of the strategy, which we want to test
                setProjectId("p1").
                setUser("user").
                setPasswordOrAPIToken("pwd").
                build();

        /* test */
        assertNotNull(result);
        PDSAdapterConfigData pdsAdapterConfigData = result.getPDSAdapterConfigData();

        assertEquals(8154711L,pdsAdapterConfigData.getBinariesTarFileSizeInBytesOrNull());
        assertEquals(binariesInputStream,pdsAdapterConfigData.getBinaryTarFileInputStreamOrNull());
        assertEquals("checksum1",pdsAdapterConfigData.getBinariesTarFileChecksumOrNull());
        assertTrue(pdsAdapterConfigData.isBinaryTarFileRequired());

        assertEquals(null,pdsAdapterConfigData.getSourceCodeZipFileInputStreamOrNull());
        assertEquals(null,pdsAdapterConfigData.getSourceCodeZipFileSizeInBytesOrNull());
        assertEquals(null,pdsAdapterConfigData.getSourceCodeZipFileChecksumOrNull());
        assertFalse(pdsAdapterConfigData.isSourceCodeZipFileRequired());
    }
    /* @formatter:on */

    /* @formatter:off */
    @Test
    void builder_can_create_a_sources_codescan_setup_by_using_providers() throws Exception {

        /* prepare*/
        when(contentProvider.isSourceRequired()).thenReturn(true);
        when(contentProvider.getSourceZipFileSizeOrNull()).thenReturn("1234");
        when(contentProvider.getSourceZipFileUploadChecksumOrNull()).thenReturn("checksum2");


        /* execute */
        PDSAdapterConfigurationStrategy strategy =  PDSAdapterConfigurationStrategy.builder().
                setConfigSupport(configSupport).
                setContentProvider(contentProvider).
                setInstallSetup(installSetup).
                setProductExecutorData(productExecutorData).
                setScanType(ScanType.CODE_SCAN).
                setSourceCodeZipFileInputStreamOrNull(sourceInputStream).
                build();


        PDSCodeScanConfigImpl result = PDSCodeScanConfigImpl.builder().
                configure(strategy). // this is the execution of the strategy, which we want to test
                setProjectId("p1").
                setUser("user").
                setPasswordOrAPIToken("pwd").
                build();

        /* test */
        assertNotNull(result);
        PDSAdapterConfigData pdsAdapterConfigData = result.getPDSAdapterConfigData();

        assertEquals(sourceInputStream,pdsAdapterConfigData.getSourceCodeZipFileInputStreamOrNull());
        assertEquals(1234L,pdsAdapterConfigData.getSourceCodeZipFileSizeInBytesOrNull());
        assertEquals("checksum2",pdsAdapterConfigData.getSourceCodeZipFileChecksumOrNull());
        assertTrue(pdsAdapterConfigData.isSourceCodeZipFileRequired());

        assertEquals(null, pdsAdapterConfigData.getBinaryTarFileInputStreamOrNull());
        assertEquals(null, pdsAdapterConfigData.getBinariesTarFileChecksumOrNull());
        assertEquals(null, pdsAdapterConfigData.getBinariesTarFileSizeInBytesOrNull());
        assertFalse(pdsAdapterConfigData.isBinaryTarFileRequired());
    }
    /* @formatter:on */

}
