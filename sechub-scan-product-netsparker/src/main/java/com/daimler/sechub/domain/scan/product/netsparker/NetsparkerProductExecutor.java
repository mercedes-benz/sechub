// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.netsparker;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.adapter.netsparker.NetsparkerAdapter;
import com.daimler.sechub.adapter.netsparker.NetsparkerAdapterConfig;
import com.daimler.sechub.adapter.netsparker.NetsparkerConfig;
import com.daimler.sechub.domain.scan.OneInstallSetupConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.WebLoginConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.product.AbstractWebScanProductExecutor;
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
	protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, NetsparkerInstallSetup setup,
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
			NetsparkerAdapterConfig netsparkerConfig = NetsparkerConfig.builder().
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
			String xml = netsparkerAdapter.start(netsparkerConfig);
			String projectId = context.getConfiguration().getProjectId();
			ProductResult result = new ProductResult(context.getSechubJobUUID(),projectId, getIdentifier(), xml);
			results.add(result);
		}
		return results;
	}

	@Override
	public ProductIdentifier getIdentifier() {
		return ProductIdentifier.NETSPARKER;
	}


}
