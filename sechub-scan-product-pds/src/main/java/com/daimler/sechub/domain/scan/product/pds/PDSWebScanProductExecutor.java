// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.adapter.pds.PDSAdapter;
import com.daimler.sechub.adapter.pds.PDSAdapterConfig;
import com.daimler.sechub.adapter.pds.PDSMetaDataID;
import com.daimler.sechub.adapter.pds.PDSWebScanConfig;
import com.daimler.sechub.domain.scan.OneInstallSetupConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.WebLoginConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.product.AbstractWebScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

@Service
public class PDSWebScanProductExecutor extends AbstractWebScanProductExecutor<PDSInstallSetup> {

	private static final Logger LOG = LoggerFactory.getLogger(PDSWebScanProductExecutor.class);

	@Autowired
	PDSAdapter netsparkerAdapter;

	@Autowired
	PDSInstallSetup installSetup;

	@Override
	protected PDSInstallSetup getInstallSetup() {
		return installSetup;
	}
	
	@Override
	protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, PDSInstallSetup setup,
			TargetRegistryInfo info) throws Exception{
		Set<URI> targetURIs = info.getURIs();
		if (targetURIs.isEmpty()) {
			/* no targets defined */
			return Collections.emptyList();
		}
		TargetType targetType = info.getTargetType();
		LOG.debug("Trigger netsparker adapter execution for target {} and setup {} ", targetType,setup);

		List<ProductResult> results = new ArrayList<>();
		/* NETSPARKER is not able to scan multiple targets, so we
		 * start NETSPARKER multiple times for each target URI
		 */
		for (URI targetURI: targetURIs) {
			/* @formatter:off */
		    
		    /* special behavior, because having multiple results here, we must find former result corresponding to 
		     * target URI.
		     */
		    executorContext.useFirstFormerResultHavingMetaData(PDSMetaDataID.KEY_TARGET_URI, targetURI);
		    
			PDSAdapterConfig netsparkerConfig = PDSWebScanConfig.builder().
					configure(createAdapterOptionsStrategy(context)).
				    configure(new WebLoginConfigBuilderStrategy(context)).
				    configure(new OneInstallSetupConfigBuilderStrategy(setup)).
					setTimeToWaitForNextCheckOperationInMinutes(setup.getScanResultCheckPeriodInMinutes()).
					setScanResultTimeOutInMinutes(setup.getScanResultCheckTimeOutInMinutes()).
					setTraceID(context.getTraceLogIdAsString()).
					setAgentName(setup.getAgentName()).
					setAgentGroupName(setup.getIdentifier(targetType)).
					setPolicyID(setup.getDefaultPolicyId()).
					setLicenseID(setup.getNetsparkerLicenseId()).
					setTargetURI(targetURI).build();
			/* @formatter:on */

			/* execute NETSPARKER by adapter and return product result */
			String xml = netsparkerAdapter.start(netsparkerConfig, executorContext.getCallBack());
			
			ProductResult currentProductResult = executorContext.getCurrentProductResult();
            currentProductResult.setResult(xml);
            results.add(currentProductResult);
			
		}
		return results;
	}

    @Override
	public ProductIdentifier getIdentifier() {
		return ProductIdentifier.NETSPARKER;
	}


}
