// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.pds.PDSCodeScanConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSCodeScanConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;

@Service
public class PDSCodeScanProductExecutor extends AbstractPDSProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCodeScanProductExecutor.class);

    public PDSCodeScanProductExecutor() {
        super(ProductIdentifier.PDS_CODESCAN, 1, ScanType.CODE_SCAN);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        PDSExecutorConfigSupport configSupport = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                serviceCollection);

        LOG.debug("Trigger PDS adapter execution of {} at {}", configSupport.getPDSProductIdentifier(), configSupport.getProductBaseURL());

        SecHubExecutionContext context = data.getSechubExecutionContext();

        PDSStorageContentProvider contentProvider = contentProviderFactory.createContentProvider(context, configSupport, getScanType());

        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            /* we reuse existing file upload checksum done by sechub */

            try (InputStream sourceCodeZipFileInputStreamOrNull = contentProvider.getSourceZipFileInputStreamOrNull();
                    InputStream binariesTarFileInputStreamOrNull = contentProvider.getBinariesTarFileInputStreamOrNull()) {

                /* @formatter:off */
                PDSCodeScanConfig pdsCodeScanConfig =PDSCodeScanConfigImpl.builder().
                        configure(PDSAdapterConfigurationStrategy.builder().
                                    setScanType(getScanType()).
                                    setProductExecutorData(data).
                                    setConfigSupport(configSupport).
                                    setSourceCodeZipFileInputStreamOrNull(sourceCodeZipFileInputStreamOrNull).
                                    setBinariesTarFileInputStreamOrNull(binariesTarFileInputStreamOrNull).
                                    setContentProvider(contentProvider).
                                    setInstallSetup(installSetup).
                                    build()).
                        build();
                    /* @formatter:on */

                /* inspect */
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.PDS_CODESCAN.name());
                inspection.notice(MetaDataInspection.TRACE_ID, pdsCodeScanConfig.getTraceID());

                /* we temporary store the adapter configuration - necessary for cancellation */
                data.rememberAdapterConfig(pdsCodeScanConfig);

                /* execute PDS by adapter and update product result */
                AdapterExecutionResult adapterResult = pdsAdapter.start(pdsCodeScanConfig, executorContext.getCallback());

                /* cancel not necessary - so forget it */
                data.forgetRememberedAdapterConfig();

                return updateCurrentProductResult(adapterResult, executorContext);
            }
        });
        return Collections.singletonList(result);

    }

}
