// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

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

import com.daimler.sechub.adapter.pds.PDSAdapter;
import com.daimler.sechub.adapter.pds.PDSInfraScanConfig;
import com.daimler.sechub.adapter.pds.PDSInfraScanConfigImpl;
import com.daimler.sechub.adapter.pds.PDSMetaDataID;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.AbstractInfrastructureScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

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
        PDSExecutorConfigSuppport configSupport = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),systemEnvironment);
        if (configSupport.isTargetTypeForbidden(targetType)) {
            LOG.info("pds adapter does not accept target type:{} so cancel execution");
            return Collections.emptyList();
        }
        LOG.debug("Trigger pds infra scan adapter execution for target {}", targetType);

        List<ProductResult> results = new ArrayList<>();

        Map<String, String> jobParameters = configSupport.createJobParametersToSendToPDS(context.getConfiguration());

        for (URI targetURI : targetURIs) {
            /* @formatter:off */
		    
		    /* special behavior, because having multiple results here, we must find former result corresponding to 
		     * target URI.
		     */
		    executorContext.useFirstFormerResultHavingMetaData(PDSMetaDataID.KEY_TARGET_URI, targetURI);
		    
		    PDSInfraScanConfig pdsInfraScanConfig = PDSInfraScanConfigImpl.builder().
		            configure(createAdapterOptionsStrategy(context)).

		            setTimeToWaitForNextCheckOperationInMinutes(setup.getDefaultScanResultCheckPeriodInMinutes()).
		            setScanResultTimeOutInMinutes(setup.getScanResultCheckTimeOutInMinutes()).


		            setTraceID(context.getTraceLogIdAsString()).
		            
		            setTargetIPs(info.getIPs()).
		            setTargetURIs(info.getURIs()).
		            
		            setPDSProductIdentifier(configSupport.getPDSProductIdentifier()).
		            setTrustAllCertificates(configSupport.isTrustAllCertificatesEnabled()).
		            setProductBaseUrl(configSupport.getProductBaseURL()).
		            setSecHubJobUUID(context.getSechubJobUUID()).
					setJobParameters(jobParameters).
					
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
