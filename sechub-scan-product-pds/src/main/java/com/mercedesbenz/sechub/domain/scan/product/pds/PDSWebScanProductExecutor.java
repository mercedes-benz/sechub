// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.pds.PDSAdapter;
import com.mercedesbenz.sechub.adapter.pds.PDSMetaDataID;
import com.mercedesbenz.sechub.adapter.pds.PDSWebScanConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSWebScanConfigImpl;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.WebConfigBuilderStrategy;
import com.mercedesbenz.sechub.domain.scan.WebScanNetworkLocationProvider;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.SystemEnvironment;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

@Service
public class PDSWebScanProductExecutor extends AbstractProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(PDSWebScanProductExecutor.class);

    @Autowired
    PDSAdapter pdsAdapter;

    @Autowired
    PDSInstallSetup installSetup;

    @Autowired
    SystemEnvironment systemEnvironment;

    @Autowired
    PDSResilienceConsultant pdsResilienceConsultant;

    public PDSWebScanProductExecutor() {
        super(ProductIdentifier.PDS_WEBSCAN, ScanType.WEB_SCAN);
    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(pdsResilienceConsultant);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {

        ProductExecutorContext executorContext = data.getProductExecutorContext();
        SecHubExecutionContext context = data.getSechubExecutionContext();
        NetworkTargetInfo info = data.getCurrentNetworkTargetInfo();

        /* we reuse config support created inside customize method */
        PDSExecutorConfigSuppport configSupport = (PDSExecutorConfigSuppport) data.getNetworkTargetDataProvider();
        
        URI targetURI = info.getURI();
        if (targetURI == null) {
            LOG.warn("NO target URI defined PDS web scan execution");
            return Collections.emptyList();
        }
        NetworkTargetType targetType = info.getTargetType();
        if (configSupport.isTargetTypeForbidden(targetType)) {
            LOG.info("PDS adapter does not accept target type:{} so cancel execution");
            return Collections.emptyList();
        }
        LOG.debug("Trigger PDS adapter execution for target {} ", targetType);

        List<ProductResult> results = new ArrayList<>();

        SecHubConfiguration secHubConfiguration = context.getConfiguration();

        Map<String, String> jobParameters = configSupport.createJobParametersToSendToPDS(secHubConfiguration);
        String projectId = secHubConfiguration.getProjectId();

        /* @formatter:off */
        executorContext.useFirstFormerResultHavingMetaData(PDSMetaDataID.KEY_TARGET_URI, targetURI);

        ProductResult result = resilientActionExecutor.executeResilient(() -> {
            PDSWebScanConfig pdsWebScanConfig = PDSWebScanConfigImpl.builder().
                        setPDSProductIdentifier(configSupport.getPDSProductIdentifier()).
                        setTrustAllCertificates(configSupport.isTrustAllCertificatesEnabled()).
                        setProductBaseUrl(configSupport.getProductBaseURL()).
                        setSecHubJobUUID(context.getSechubJobUUID()).

                        setSecHubConfigModel(secHubConfiguration).

                        configure(createAdapterOptionsStrategy(data)).
                        configure(new WebConfigBuilderStrategy(context)).

                        setTimeToWaitForNextCheckOperationInMilliseconds(configSupport.getTimeToWaitForNextCheckOperationInMilliseconds(installSetup)).
                        setTimeOutInMinutes(configSupport.getTimeoutInMinutes(installSetup)).

                        setUser(configSupport.getUser()).
                        setPasswordOrAPIToken(configSupport.getPasswordOrAPIToken()).
                        setProjectId(projectId).

                        setTraceID(context.getTraceLogIdAsString()).
                        setJobParameters(jobParameters).

                        setTargetURI(targetURI).

                        build();
            /* @formatter:on */

            /* execute PDS by adapter and return product result */
            String pdsResult = pdsAdapter.start(pdsWebScanConfig, executorContext.getCallback());

            ProductResult currentProductResult = executorContext.getCurrentProductResult();
            currentProductResult.setResult(pdsResult);
            return currentProductResult;
        });
        results.add(result);

        return results;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    protected void customize(ProductExecutorData data) {
        SecHubConfiguration secHubConfiguration = data.getSechubExecutionContext().getConfiguration();
        data.setNetworkLocationProvider(new WebScanNetworkLocationProvider(secHubConfiguration));
        
        ProductExecutorContext executorContext = data.getProductExecutorContext();
        PDSExecutorConfigSuppport configSupport = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                systemEnvironment);

        data.setNetworkTargetDataProvider(configSupport);
        
    }

}
