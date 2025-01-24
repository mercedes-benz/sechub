// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.adapter.AdapterConfig;
import com.mercedesbenz.sechub.adapter.AdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.AdapterConfigurationStrategy;
import com.mercedesbenz.sechub.commons.core.ConfigurationFailureException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorData;

/**
 * This strategy will configure
 * <ul>
 * <li>adapter options</li> - delegated to
 * {@link SecHubAdapterOptionsBuilderStrategy}
 * <li>product base url</li> - from config support
 * <li>user</li> - from config support
 * <li>apiTokenOrPassword</li> - from config support
 * <li>projectId</li>
 * <li>traceId</li>
 *
 * <ul>
 *
 * @author Albert Tregnaghi
 *
 */
public class DefaultAdapterConfigurationStrategy implements AdapterConfigurationStrategy {

    private ScanType scanType;

    private ProductExecutorData data;

    private DefaultExecutorConfigSupport configSupport;

    /**
     * Creates a {@link DefaultAdapterConfigurationStrategy}
     *
     * @param data          product exeuctor data
     * @param configSupport default configuration support
     * @param scanType      scan type to use
     */
    public DefaultAdapterConfigurationStrategy(ProductExecutorData data, DefaultExecutorConfigSupport configSupport, ScanType scanType) {
        this.data = data;
        this.configSupport = configSupport;
        this.scanType = scanType;
    }

    @Override
    public <B extends AdapterConfigBuilder, C extends AdapterConfig> void configure(B configBuilder) throws ConfigurationFailureException {
        /* @formatter:off */
        SecHubExecutionContext context = data.getSechubExecutionContext();
        String projectId = context.getConfiguration().getProjectId();
        configBuilder.configure(new SecHubAdapterOptionsBuilderStrategy(data, scanType));

        configBuilder.setProductBaseUrl(configSupport.getProductBaseURL());

        configBuilder.setUser(configSupport.getUser());
        configBuilder.setPasswordOrAPIToken(configSupport.getPasswordOrAPIToken());

        configBuilder.setProjectId(projectId);
        configBuilder.setTraceID(context.getTraceLogIdAsString());
		/* @formatter:on */

    }

}
