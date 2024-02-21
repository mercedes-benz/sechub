package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.pds.PDSPrepareConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSPrepareConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.metadata.MetaDataInspection;

@Service
public class PDSPrepareExecutor extends AbstractPDSProductExecutor {

    public PDSPrepareExecutor() {
        super(ProductIdentifier.PDS_PREPARE, 1, ScanType.PREPARE);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data, PDSExecutorConfigSupport configSupport, PDSStorageContentProvider contentProvider)
            throws Exception {
        // TODO prepare phase
        ProductExecutorContext executorContext = data.getProductExecutorContext();

        ProductResult result = resilientActionExecutor.executeResilient(() -> {

            try (InputStream sourceCodeZipFileInputStreamOrNull = contentProvider.getSourceZipFileInputStreamOrNull();
                    InputStream binariesTarFileInputStreamOrNull = contentProvider.getBinariesTarFileInputStreamOrNull()) { /* @formatter:off */

                PDSPrepareConfig pdsPrepareConfig = PDSPrepareConfigImpl.builder().
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
                MetaDataInspection inspection = scanMetaDataCollector.inspect(ProductIdentifier.PDS_PREPARE.name());
                inspection.notice(MetaDataInspection.TRACE_ID, pdsPrepareConfig.getTraceID());

                /* we temporary store the adapter configuration - necessary for cancellation */
                data.rememberAdapterConfig(pdsPrepareConfig);

                /* execute PDS by adapter and update product result */
                AdapterExecutionResult adapterResult = pdsAdapter.start(pdsPrepareConfig, executorContext.getCallback());

                /* cancel not necessary - so forget it */
                data.forgetRememberedAdapterConfig();

                return updateCurrentProductResult(adapterResult, executorContext);
            }
        });
        return Collections.singletonList(result);

    }
}
