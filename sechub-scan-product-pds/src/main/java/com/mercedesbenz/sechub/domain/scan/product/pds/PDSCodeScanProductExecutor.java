// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.adapter.pds.PDSAdapter;
import com.mercedesbenz.sechub.adapter.pds.PDSCodeScanConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSCodeScanConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspector;

@Service
public class PDSCodeScanProductExecutor extends AbstractProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(PDSCodeScanProductExecutor.class);

    @Autowired
    PDSAdapter pdsAdapter;

    @Autowired
    PDSInstallSetup installSetup;

    @Autowired
    PDSExecutorConfigSuppportServiceCollection serviceCollection;

    @Autowired
    MetaDataInspector scanMetaDataCollector;

    @Autowired
    PDSResilienceConsultant pdsResilienceConsultant;

    @Autowired
    PDSStorageContentProviderFactory contentProviderFactory;

    @Autowired
    MockDataIdentifierFactory mockDataIdentifierFactory;

    public PDSCodeScanProductExecutor() {
        super(ProductIdentifier.PDS_CODESCAN, 1, ScanType.CODE_SCAN);
    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(pdsResilienceConsultant);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {
        LOG.debug("Trigger PDS adapter execution");

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        PDSExecutorConfigSuppport configSupport = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                serviceCollection);

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

                /* execute PDS by adapter and update product result */
                AdapterExecutionResult adapterResult = pdsAdapter.start(pdsCodeScanConfig, executorContext.getCallback());

                return updateCurrentProductResult(adapterResult, executorContext);
            }
        });
        return Collections.singletonList(result);

    }

    @Override
    protected void customize(ProductExecutorData data) {

    }

}
