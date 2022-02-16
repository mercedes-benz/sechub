// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

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

import com.daimler.sechub.adapter.pds.PDSAdapter;
import com.daimler.sechub.adapter.pds.PDSMetaDataID;
import com.daimler.sechub.adapter.pds.PDSWebScanConfig;
import com.daimler.sechub.adapter.pds.PDSWebScanConfigImpl;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.WebConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.product.AbstractWebScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.resilience.ResilientActionExecutor;

@Service
public class PDSWebScanProductExecutor extends AbstractWebScanProductExecutor<PDSInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSWebScanProductExecutor.class);

    @Autowired
    PDSAdapter pdsAdapter;

    @Autowired
    PDSInstallSetup installSetup;

    @Autowired
    SystemEnvironment systemEnvironment;

    @Autowired
    PDSResilienceConsultant pdsResilienceConsultant;

    ResilientActionExecutor<ProductResult> resilientActionExecutor;

    public PDSWebScanProductExecutor() {
        /* we create here our own instance - only for this service! */
        this.resilientActionExecutor = new ResilientActionExecutor<>();
    }

    @PostConstruct
    protected void postConstruct() {
        this.resilientActionExecutor.add(pdsResilienceConsultant);
    }

    @Override
    protected PDSInstallSetup getInstallSetup() {
        return installSetup;
    }

    @Override
    protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, PDSInstallSetup setup,
            TargetRegistryInfo info) throws Exception {

        PDSExecutorConfigSuppport configSupport = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                systemEnvironment);

        URI targetURI = info.getURI();
        if (targetURI == null) {
            /* no targets defined */
            return Collections.emptyList();
        }
        TargetType targetType = info.getTargetType();
        if (configSupport.isTargetTypeForbidden(targetType)) {
            LOG.info("pds adapter does not accept target type:{} so cancel execution");
            return Collections.emptyList();
        }
        LOG.debug("Trigger PDS adapter execution for target {} ", targetType);

        List<ProductResult> results = new ArrayList<>();

        String projectId = context.getConfiguration().getProjectId();

        Map<String, String> jobParameters = configSupport.createJobParametersToSendToPDS(context.getConfiguration());

        /* @formatter:off */
        executorContext.useFirstFormerResultHavingMetaData(PDSMetaDataID.KEY_TARGET_URI, targetURI);

        ProductResult result = resilientActionExecutor.executeResilient(() -> {
            PDSWebScanConfig pdsWebScanConfig = PDSWebScanConfigImpl.builder().
                        setPDSProductIdentifier(configSupport.getPDSProductIdentifier()).
                        setTrustAllCertificates(configSupport.isTrustAllCertificatesEnabled()).
                        setProductBaseUrl(configSupport.getProductBaseURL()).
                        setSecHubJobUUID(context.getSechubJobUUID()).

                        setSecHubConfigModel(context.getConfiguration()).

                        configure(createAdapterOptionsStrategy(context)).
                        configure(new WebConfigBuilderStrategy(context)).

                        setTimeToWaitForNextCheckOperationInMilliseconds(configSupport.getTimeToWaitForNextCheckOperationInMilliseconds(setup)).
                        setTimeOutInMinutes(configSupport.getTimeoutInMinutes(setup)).

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
    public ProductIdentifier getIdentifier() {
        return ProductIdentifier.PDS_WEBSCAN;
    }

    @Override
    public int getVersion() {
        return 1;
    }

}
