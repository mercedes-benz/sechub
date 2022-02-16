// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.pds.PDSAdapter;
import com.mercedesbenz.sechub.adapter.pds.PDSInfraScanConfig;
import com.mercedesbenz.sechub.adapter.pds.PDSInfraScanConfigImpl;
import com.mercedesbenz.sechub.adapter.pds.PDSMetaDataID;
import com.mercedesbenz.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.mercedesbenz.sechub.domain.scan.TargetType;
import com.mercedesbenz.sechub.domain.scan.product.AbstractInfrastructureScanProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.SystemEnvironment;
import com.mercedesbenz.sechub.sharedkernel.execution.SecHubExecutionContext;

@Service
public class PDSInfraScanProductExecutor extends AbstractInfrastructureScanProductExecutor<PDSInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSInfraScanProductExecutor.class);

    @Autowired
    PDSAdapter pdsAdapter;

    @Autowired
    PDSInstallSetup installSetup;

    @Autowired
    SystemEnvironment systemEnvironment;

    @Override
    protected PDSInstallSetup getInstallSetup() {
        return installSetup;
    }

    @Override
    protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, PDSInstallSetup setup,
            TargetRegistryInfo info) throws Exception {

        Set<URI> targetURIs = info.getURIs();
        if (targetURIs.isEmpty()) {
            /* no targets defined */
            return Collections.emptyList();
        }
        TargetType targetType = info.getTargetType();
        PDSExecutorConfigSuppport configSupport = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),
                systemEnvironment);
        if (configSupport.isTargetTypeForbidden(targetType)) {
            LOG.info("pds adapter does not accept target type:{} so cancel execution");
            return Collections.emptyList();
        }
        LOG.debug("Trigger pds infra scan adapter execution for target {}", targetType);

        List<ProductResult> results = new ArrayList<>();

        Map<String, String> jobParameters = configSupport.createJobParametersToSendToPDS(context.getConfiguration());
        String projectId = context.getConfiguration().getProjectId();

        for (URI targetURI : targetURIs) {
            /* @formatter:off */

            /* special behavior, because having multiple results here, we must find former result corresponding to
             * target URI.
             */
            executorContext.useFirstFormerResultHavingMetaData(PDSMetaDataID.KEY_TARGET_URI, targetURI);

            PDSInfraScanConfig pdsInfraScanConfig = PDSInfraScanConfigImpl.builder().
                    setPDSProductIdentifier(configSupport.getPDSProductIdentifier()).
                    setTrustAllCertificates(configSupport.isTrustAllCertificatesEnabled()).
                    setProductBaseUrl(configSupport.getProductBaseURL()).
                    setSecHubJobUUID(context.getSechubJobUUID()).

                    setSecHubConfigModel(context.getConfiguration()).

                    configure(createAdapterOptionsStrategy(context)).

                    setTimeToWaitForNextCheckOperationInMilliseconds(configSupport.getTimeToWaitForNextCheckOperationInMilliseconds(setup)).
                    setTimeOutInMinutes(configSupport.getTimeoutInMinutes(setup)).

                    setUser(configSupport.getUser()).
                    setPasswordOrAPIToken(configSupport.getPasswordOrAPIToken()).
                    setProjectId(projectId).

                    setTraceID(context.getTraceLogIdAsString()).
                    setJobParameters(jobParameters).

                    setTargetIPs(info.getIPs()).
                    setTargetURIs(info.getURIs()).

                    build();
            /* @formatter:on */

            /* execute PDS by adapter and return product result */
            String xml = pdsAdapter.start(pdsInfraScanConfig, executorContext.getCallback());

            ProductResult currentProductResult = executorContext.getCurrentProductResult();
            currentProductResult.setResult(xml);
            results.add(currentProductResult);

        }
        return results;
    }

    @Override
    public ProductIdentifier getIdentifier() {
        return ProductIdentifier.PDS_INFRASCAN;
    }

    @Override
    public int getVersion() {
        return 1;
    }

}
