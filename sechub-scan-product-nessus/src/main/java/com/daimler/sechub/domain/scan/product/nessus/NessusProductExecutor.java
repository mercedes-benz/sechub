// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.nessus;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.nessus.NessusAdapter;
import com.daimler.sechub.adapter.nessus.NessusAdapterConfig;
import com.daimler.sechub.adapter.nessus.NessusConfig;
import com.daimler.sechub.domain.scan.TargetIdentifyingMultiInstallSetupConfigBuilderStrategy;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.AbstractInfrastructureScanProductExecutor;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

@Service
public class NessusProductExecutor extends AbstractInfrastructureScanProductExecutor<NessusInstallSetup> {

    private static final Logger LOG = LoggerFactory.getLogger(NessusProductExecutor.class);

    @Value("${sechub.adapter.nessus.proxy.hostname:}")
    @MustBeDocumented("Proxy hostname for nessus server connection, when empty no proxy is used. When not empty proxy port must be set too!")
    String proxyHostname;

    @Value("${sechub.adapter.nessus.proxy.port:0}")
    @MustBeDocumented("Proxy port for nessus server connection, default is 0. If you are setting a proxy hostname you have to configure this value correctly")
    int proxyPort;

    @Value("${sechub.adapter.nessus.scanresultcheck.period.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
    private int scanResultCheckPeriodInMinutes;

    @Value("${sechub.adapter.nessus.scanresultcheck.timeout.minutes:-1}")
    @MustBeDocumented(AbstractAdapterConfigBuilder.DOCUMENT_INFO_TIMEOUT)
    private int scanResultCheckTimeOutInMinutes;

    @Autowired
    NessusAdapter nessusAdapter;

    @Autowired
    NessusInstallSetup installSetup;

    @Override
    protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, NessusInstallSetup setup,
            TargetRegistryInfo data) throws Exception {
        if (data.getURIs().isEmpty() && data.getIPs().isEmpty()) {
            return Collections.emptyList();
        }
        TargetType targetType = data.getTargetType();
        LOG.debug("Trigger nessus adapter execution for target type {} and setup {} ", targetType, setup);
        /* @formatter:off */
		NessusAdapterConfig nessusConfig = NessusConfig.builder().
				configure(createAdapterOptionsStrategy(context)).
				configure(new TargetIdentifyingMultiInstallSetupConfigBuilderStrategy(setup,targetType)).
				setTimeToWaitForNextCheckOperationInMinutes(scanResultCheckPeriodInMinutes).
				setScanResultTimeOutInMinutes(scanResultCheckTimeOutInMinutes).
				setProxyHostname(proxyHostname).
				setProxyPort(proxyPort).
				setTraceID(context.getTraceLogIdAsString()).
				setPolicyID(setup.getDefaultPolicyId()).
				setTargetIPs(data.getIPs()).
				setTargetURIs(data.getURIs()).build();
		/* @formatter:on */

        /* execute NESSUS by adapter and return product result */
        String xml = nessusAdapter.start(nessusConfig, executorContext.getCallback());
        
        ProductResult productResult = executorContext.getCurrentProductResult(); // product result is set by callback
        productResult.setResult(xml);
        return Collections.singletonList(productResult);
    }

    @Override
    public ProductIdentifier getIdentifier() {
        return ProductIdentifier.NESSUS;
    }

    @Override
    protected NessusInstallSetup getInstallSetup() {
        return installSetup;
    }
    
    protected List<InetAddress> resolveInetAdressForTarget(SecHubConfiguration config) {
        return super.resolveInetAdressForTarget(config);
    }


    @Override
    public int getVersion() {
        return 1;
    }

}
