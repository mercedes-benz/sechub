// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.pds.PDSMetaDataID;
import com.mercedesbenz.sechub.adapter.pds.PDSWebScanConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSWebScanConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetProductServerDataAdapterConfigurationStrategy;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.WebConfigBuilderStrategy;
import com.mercedesbenz.sechub.domain.scan.WebScanNetworkLocationProvider;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

@Service
public class PDSWebScanProductExecutor extends AbstractPDSProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(PDSWebScanProductExecutor.class);

    public PDSWebScanProductExecutor() {
        super(ProductIdentifier.PDS_WEBSCAN, 1, ScanType.WEB_SCAN);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data, PDSExecutorConfigSupport defaultConfigSupport,
            PDSStorageContentProvider contentProvider) throws Exception {

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        SecHubExecutionContext context = data.getSechubExecutionContext();
        NetworkTargetInfo info = data.getCurrentNetworkTargetInfo();

        /* we reuse config support created inside customize method */
        PDSExecutorConfigSupport configSupport = (PDSExecutorConfigSupport) data.getNetworkTargetDataProvider();

        URI targetURI = info.getURI();
        if (targetURI == null) {
            LOG.warn("NO target URI defined for PDS web scan execution");
            return Collections.emptyList();
        }
        NetworkTargetType targetType = info.getTargetType();
        if (configSupport.isTargetTypeForbidden(targetType)) {
            LOG.info("PDS adapter does not accept target type:{} so cancel execution");
            return Collections.emptyList();
        }
        LOG.debug("Trigger PDS adapter execution for target {} ", targetType);

        List<ProductResult> results = new ArrayList<>();

        /* @formatter:off */
        executorContext.useFirstFormerResultHavingMetaData(PDSMetaDataID.KEY_TARGET_URI, targetURI);

            ProductResult result = resilientActionExecutor.executeResilient(() -> {
                try (InputStream sourceCodeZipFileInputStreamOrNull = contentProvider.getSourceZipFileInputStreamOrNull()){
                PDSWebScanConfig pdsWebScanConfig = PDSWebScanConfigImpl.builder().
                        configure(PDSAdapterConfigurationStrategy.builder().
                                setScanType(getScanType()).
                                setProductExecutorData(data).
                                setConfigSupport(configSupport).
                                setSourceCodeZipFileInputStreamOrNull(sourceCodeZipFileInputStreamOrNull).
                                setContentProvider(contentProvider).
                                setInstallSetup(installSetup).
                                build()).
                            /* additional: */
                            configure(new WebConfigBuilderStrategy(context)).
                            configure(new NetworkTargetProductServerDataAdapterConfigurationStrategy(configSupport,data.getCurrentNetworkTargetInfo().getTargetType())).

                            setTargetURI(targetURI).
                            setTargetType(info.getTargetType().name()).

                            build();
                /* @formatter:on */

                /* we temporary store the adapter configuration - necessary for cancellation */
                data.rememberAdapterConfig(pdsWebScanConfig);

                /* execute PDS by adapter and return product result */
                AdapterExecutionResult adapterResult = pdsAdapter.start(pdsWebScanConfig, executorContext.getCallback());

                /* cancel not necessary - so forget it */
                data.forgetRememberedAdapterConfig();

                return updateCurrentProductResult(adapterResult, executorContext);
            }

        });
        results.add(result);

        return results;
    }

    @Override
    protected void customize(ProductExecutorData data) {
        SecHubConfiguration secHubConfiguration = data.getSechubExecutionContext().getConfiguration();
        data.setNetworkLocationProvider(new WebScanNetworkLocationProvider(secHubConfiguration));

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        PDSExecutorConfigSupport configSupport = PDSExecutorConfigSupport.createSupportAndAssertConfigValid(executorContext, serviceCollection);

        data.setNetworkTargetDataProvider(configSupport);

    }

}
