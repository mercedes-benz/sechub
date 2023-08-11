// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.pds.PDSSecretScanConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSSecretScanConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;

@Service
public class PDSSecretScanProductExecutor extends AbstractPDSProductExecutor {

    public PDSSecretScanProductExecutor() {
        super(ProductIdentifier.PDS_SECRETSCAN, 1, ScanType.SECRET_SCAN);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data, PDSExecutorConfigSupport configSupport, PDSStorageContentProvider contentProvider)
            throws Exception {
        ProductExecutorContext executorContext = data.getProductExecutorContext();

        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            try (InputStream sourceCodeZipFileInputStreamOrNull = contentProvider.getSourceZipFileInputStreamOrNull();
                    InputStream binariesTarFileInputStreamOrNull = contentProvider.getBinariesTarFileInputStreamOrNull()) { /* @formatter:off */

                 PDSSecretScanConfig pdsSecretScanConfig = PDSSecretScanConfigImpl.builder().
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
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.PDS_SECRETSCAN.name());
                inspection.notice(MetaDataInspection.TRACE_ID, pdsSecretScanConfig.getTraceID());

                /* we temporary store the adapter configuration - necessary for cancellation */
                data.rememberAdapterConfig(pdsSecretScanConfig);

                /* execute PDS by adapter and update product result */
                AdapterExecutionResult adapterResult = pdsAdapter.start(pdsSecretScanConfig, executorContext.getCallback());

                /* cancel not necessary - so forget it */
                data.forgetRememberedAdapterConfig();

                return updateCurrentProductResult(adapterResult, executorContext);
            }
        });
        return Collections.singletonList(result);

    }

}
