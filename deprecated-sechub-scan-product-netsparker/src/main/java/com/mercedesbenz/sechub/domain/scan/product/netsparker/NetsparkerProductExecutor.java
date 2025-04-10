// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.netsparker;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.adapter.netsparker.NetsparkerAdapter;
import com.mercedesbenz.sechub.adapter.netsparker.NetsparkerAdapterConfig;
import com.mercedesbenz.sechub.adapter.netsparker.NetsparkerConfig;
import com.mercedesbenz.sechub.adapter.netsparker.NetsparkerMetaDataID;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetProductServerDataAdapterConfigurationStrategy;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.SecHubAdapterOptionsBuilderStrategy;
import com.mercedesbenz.sechub.domain.scan.WebConfigBuilderStrategy;
import com.mercedesbenz.sechub.domain.scan.WebScanNetworkLocationProvider;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

@Deprecated // will be removed in future
@Service
public class NetsparkerProductExecutor extends AbstractProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(NetsparkerProductExecutor.class);

    @Autowired
    NetsparkerAdapter netsparkerAdapter;

    @Autowired
    NetsparkerInstallSetup installSetup;

    @Autowired
    MockDataIdentifierFactory mockDataIdentifierFactory;

    public NetsparkerProductExecutor() {
        super(ProductIdentifier.NETSPARKER, 1);
    }

    @Override
    protected void customize(ProductExecutorData data) {
        SecHubConfiguration sechubConfiguration = data.getSechubExecutionContext().getConfiguration();
        data.setNetworkLocationProvider(new WebScanNetworkLocationProvider(sechubConfiguration));
        data.setNetworkTargetDataProvider(installSetup);

    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {
        NetworkTargetInfo info = data.getCurrentNetworkTargetInfo();
        URI targetURI = info.getURI();
        if (targetURI == null) {
            /* no targets defined */
            return Collections.emptyList();
        }
        NetworkTargetType targetType = info.getTargetType();
        LOG.debug("Trigger netsparker adapter execution for target {}", targetType);

        List<ProductResult> results = new ArrayList<>();

        /* NETSPARKER is not able to scan multiple targets */

        /*
         * special behavior, because having multiple results here, we must find former
         * result corresponding to target URI.
         */
        /* @formatter:off */
		ProductExecutorContext productExecutorContext = data.getProductExecutorContext();
        productExecutorContext.useFirstFormerResultHavingMetaData(NetsparkerMetaDataID.KEY_TARGET_URI, targetURI);

        NetsparkerAdapterConfig netsparkerConfig = NetsparkerConfig.builder().
				configure(new SecHubAdapterOptionsBuilderStrategy(data, getScanType())).
				configure(new WebConfigBuilderStrategy(data.getSechubExecutionContext())).
				configure(new NetworkTargetProductServerDataAdapterConfigurationStrategy(installSetup, targetType)).
				setTimeToWaitForNextCheckOperationInMinutes(installSetup.getScanResultCheckPeriodInMinutes()).
				setTimeOutInMinutes(installSetup.getScanResultCheckTimeOutInMinutes()).
				setTraceID(data.getTraceLogIdAsString()).
				setAgentName(installSetup.getAgentName()).
				setAgentGroupName(data.getNetworkTargetProductServerDataSupport().getIdentifier(targetType)).
				setPolicyID(installSetup.getDefaultPolicyId()).
				setLicenseID(installSetup.getNetsparkerLicenseId()).
				setTargetType(info.getTargetType().name()).
				setMockDataIdentifier(data.getMockDataIdentifier()).
				setTargetURI(targetURI).build();
		/* @formatter:on */

        /* execute NETSPARKER by adapter and return product result */
        AdapterExecutionResult adapterResult = netsparkerAdapter.start(netsparkerConfig, productExecutorContext.getCallback());

        ProductResult currentProductResult = updateCurrentProductResult(adapterResult, productExecutorContext);
        results.add(currentProductResult);

        return results;
    }
}
