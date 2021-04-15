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

@Service
public class PDSWebScanProductExecutor extends AbstractWebScanProductExecutor<PDSInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSWebScanProductExecutor.class);

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
        
        PDSExecutorConfigSuppport configSupport = PDSExecutorConfigSuppport.createSupportAndAssertConfigValid(executorContext.getExecutorConfig(),systemEnvironment);
        
        Set<URI> targetURIs = info.getURIs();
        if (targetURIs.isEmpty()) {
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

        Map<String, String> jobParameters = configSupport.createJobParametersToSendToPDS();
        /* we currently scan always only ONE url at the same time */
        for (URI targetURI : targetURIs) {
            /* @formatter:off */
		    
		    /* special behavior, because having multiple results here, we must find former result corresponding to 
		     * target URI.
		     */
		    executorContext.useFirstFormerResultHavingMetaData(PDSMetaDataID.KEY_TARGET_URI, targetURI);
		    
			PDSWebScanConfig pdsWebScanConfig = PDSWebScanConfigImpl.builder().
			        setTrustAllCertificates(configSupport.isTrustAllCertificatesEnabled()).
			        setPDSProductIdentifier(configSupport.getPDSProductIdentifier()).
			        setProductBaseUrl(configSupport.getProductBaseURL()).
			        setSecHubJobUUID(context.getSechubJobUUID()).
					configure(createAdapterOptionsStrategy(context)).
				    configure(new WebConfigBuilderStrategy(context)).
					setTimeToWaitForNextCheckOperationInMinutes(setup.getDefaultScanResultCheckPeriodInMinutes()).
					setScanResultTimeOutInMinutes(setup.getScanResultCheckTimeOutInMinutes()).
					setTraceID(context.getTraceLogIdAsString()).
					setJobParameters(jobParameters).
					setTargetURI(targetURI).build();
			/* @formatter:on */

            /* execute PDS by adapter and return product result */
            String result = pdsAdapter.start(pdsWebScanConfig, executorContext.getCallback());

            ProductResult currentProductResult = executorContext.getCurrentProductResult();
            currentProductResult.setResult(result);
            results.add(currentProductResult);

        }
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
