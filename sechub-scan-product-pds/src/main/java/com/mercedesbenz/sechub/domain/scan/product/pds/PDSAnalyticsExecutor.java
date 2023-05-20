// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.pds.PDSAnalyticsConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSAnalyticsConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;

@Service
public class PDSAnalyticsExecutor extends AbstractPDSProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(PDSAnalyticsExecutor.class);

    public PDSAnalyticsExecutor() {
        super(ProductIdentifier.PDS_ANALYTICS, 1, ScanType.ANALYTICS);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {
        LOG.debug("Trigger PDS adapter execution");

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        PDSExecutorConfigSupport configSupport = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                serviceCollection);

        SecHubExecutionContext context = data.getSechubExecutionContext();

        PDSStorageContentProvider contentProvider = contentProviderFactory.createContentProvider(context, configSupport, getScanType());

        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            /* we reuse existing file upload checksum done by sechub */

            try (InputStream sourceCodeZipFileInputStreamOrNull = contentProvider.getSourceZipFileInputStreamOrNull();
                    InputStream binariesTarFileInputStreamOrNull = contentProvider.getBinariesTarFileInputStreamOrNull()) {

                /* @formatter:off */

                PDSAnalyticsConfig pdsAnalyticsConfig = PDSAnalyticsConfigImpl.builder().
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
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.PDS_ANALYTICS.name());
                inspection.notice(MetaDataInspection.TRACE_ID, pdsAnalyticsConfig.getTraceID());

                /* we temporary store the adapter configuration - necessary for cancellation */
                data.rememberAdapterConfig(pdsAnalyticsConfig);

                /* execute PDS by adapter and update product result */
                AdapterExecutionResult adapterResult = pdsAdapter.start(pdsAnalyticsConfig, executorContext.getCallback());

                /* cancel not necessary - so forget it */
                data.forgetRememberedAdapterConfig();

                return updateCurrentProductResult(adapterResult, executorContext);
            }
        });
        return Collections.singletonList(result);

    }

}
