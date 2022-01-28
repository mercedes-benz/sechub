// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.netsparker;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.adapter.netsparker.NetsparkerAdapter;
import com.daimler.sechub.adapter.netsparker.NetsparkerAdapterConfig;
import com.daimler.sechub.adapter.netsparker.NetsparkerConfig;
import com.daimler.sechub.adapter.netsparker.NetsparkerMetaDataID;
import com.daimler.sechub.domain.scan.OneInstallSetupConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.WebConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.product.AbstractWebScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

@Service
public class NetsparkerProductExecutor extends AbstractWebScanProductExecutor<NetsparkerInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(NetsparkerProductExecutor.class);

    @Autowired
    NetsparkerAdapter netsparkerAdapter;

    @Autowired
    NetsparkerInstallSetup installSetup;

    @Override
    protected NetsparkerInstallSetup getInstallSetup() {
        return installSetup;
    }

    @Override
    protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, NetsparkerInstallSetup setup,
            TargetRegistryInfo info) throws Exception {

        URI targetURI = info.getURI();
        if (targetURI == null) {
            /* no targets defined */
            return Collections.emptyList();
        }
        TargetType targetType = info.getTargetType();
        LOG.debug("Trigger netsparker adapter execution for target {} and setup {} ", targetType, setup);

        List<ProductResult> results = new ArrayList<>();

        /* NETSPARKER is not able to scan multiple targets */

        /*
         * special behavior, because having multiple results here, we must find former
         * result corresponding to target URI.
         */
        /* @formatter:off */
		executorContext.useFirstFormerResultHavingMetaData(NetsparkerMetaDataID.KEY_TARGET_URI, targetURI);
		    
		NetsparkerAdapterConfig netsparkerConfig = NetsparkerConfig.builder().
				configure(createAdapterOptionsStrategy(context)).
				configure(new WebConfigBuilderStrategy(context)).
				configure(new OneInstallSetupConfigBuilderStrategy(setup)).
				setTimeToWaitForNextCheckOperationInMinutes(setup.getScanResultCheckPeriodInMinutes()).
				setTimeOutInMinutes(setup.getScanResultCheckTimeOutInMinutes()).
				setTraceID(context.getTraceLogIdAsString()).
				setAgentName(setup.getAgentName()).
				setAgentGroupName(setup.getIdentifier(targetType)).
				setPolicyID(setup.getDefaultPolicyId()).
				setLicenseID(setup.getNetsparkerLicenseId()).
				setTargetURI(targetURI).build();
		/* @formatter:on */

        /* execute NETSPARKER by adapter and return product result */
        String xml = netsparkerAdapter.start(netsparkerConfig, executorContext.getCallback());

        ProductResult currentProductResult = executorContext.getCurrentProductResult();
        currentProductResult.setResult(xml);
        results.add(currentProductResult);

        return results;
    }

    @Override
    public ProductIdentifier getIdentifier() {
        return ProductIdentifier.NETSPARKER;
    }

    @Override
    public int getVersion() {
        return 1;
    }

}
