// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.nessus;

import static com.mercedesbenz.sechub.domain.scan.product.nessus.NessusConstants.*;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.nessus.NessusAdapter;
import com.mercedesbenz.sechub.adapter.nessus.NessusAdapterConfig;
import com.mercedesbenz.sechub.adapter.nessus.NessusConfig;
import com.mercedesbenz.sechub.commons.core.doc.MustBeDocumented;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.InfraScanNetworkLocationProvider;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetProductServerDataAdapterConfigurationStrategy;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetRegistry.NetworkTargetInfo;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;
import com.mercedesbenz.sechub.domain.scan.SecHubAdapterOptionsBuilderStrategy;
import com.mercedesbenz.sechub.domain.scan.product.AbstractProductExecutor;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

@Deprecated // will be removed in future
@Service
public class NessusProductExecutor extends AbstractProductExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(NessusProductExecutor.class);

    @Value("${sechub.adapter.nessus.proxy.hostname:}")
    @MustBeDocumented(value = "Proxy hostname for nessus server connection, when empty no proxy is used. When not empty proxy port must be set too!", scope = SCOPE_NESSUS)
    String proxyHostname;

    @Value("${sechub.adapter.nessus.proxy.port:0}")
    @MustBeDocumented(value = "Proxy port for nessus server connection, default is 0. If you are setting a proxy hostname you have to configure this value correctly", scope = SCOPE_NESSUS)
    int proxyPort;

    @Value("${sechub.adapter.nessus.scanresultcheck.period.minutes:-1}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT_IN_MINUTES, scope = SCOPE_NESSUS)
    private int scanResultCheckPeriodInMinutes;

    @Value("${sechub.adapter.nessus.scanresultcheck.timeout.minutes:-1}")
    @MustBeDocumented(value = AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT_IN_MINUTES, scope = SCOPE_NESSUS)
    private int scanResultCheckTimeOutInMinutes;

    @Autowired
    NessusAdapter nessusAdapter;

    @Autowired
    NessusInstallSetup installSetup;

    public NessusProductExecutor() {
        super(ProductIdentifier.NESSUS, 1, ScanType.INFRA_SCAN);
    }

    @Override
    protected List<ProductResult> executeByAdapter(ProductExecutorData data) throws Exception {
        NetworkTargetInfo info = data.getCurrentNetworkTargetInfo();

        if (info.getURIs().isEmpty() && info.getIPs().isEmpty()) {
            LOG.debug("{} Nessus scan not possible, because no uri or ip defined", data.getTraceLogId());
            return Collections.emptyList();
        }

        NetworkTargetType targetType = info.getTargetType();
        LOG.debug("Trigger Nessus adapter execution for target type {}", targetType);

        /* @formatter:off */
		NessusAdapterConfig nessusConfig = NessusConfig.builder().
				configure(new SecHubAdapterOptionsBuilderStrategy(data, getScanType())).
				configure(new NetworkTargetProductServerDataAdapterConfigurationStrategy(installSetup,targetType)).
				setTimeToWaitForNextCheckOperationInMinutes(scanResultCheckPeriodInMinutes).
				setTimeOutInMinutes(scanResultCheckTimeOutInMinutes).
				setProxyHostname(proxyHostname).
				setProxyPort(proxyPort).
				setTraceID(data.getTraceLogIdAsString()).
				setPolicyID(installSetup.getDefaultPolicyId()).
				setMockDataIdentifier(data.getMockDataIdentifier()).
				setTargetIPs(info.getIPs()).
				setTargetURIs(info.getURIs()).build();
		/* @formatter:on */

        /* execute NESSUS by adapter and return product result */
        ProductExecutorContext productExecutorContext = data.getProductExecutorContext();
        AdapterExecutionResult adapterResult = nessusAdapter.start(nessusConfig, productExecutorContext.getCallback());

        ProductResult productResult = updateCurrentProductResult(adapterResult, productExecutorContext);
        return Collections.singletonList(productResult);
    }

    @Override
    protected void customize(ProductExecutorData data) {
        SecHubConfiguration secHubConfiguration = data.getSechubExecutionContext().getConfiguration();

        data.setNetworkLocationProvider(new InfraScanNetworkLocationProvider(secHubConfiguration));
        data.setNetworkTargetDataProvider(installSetup);
    }

}
