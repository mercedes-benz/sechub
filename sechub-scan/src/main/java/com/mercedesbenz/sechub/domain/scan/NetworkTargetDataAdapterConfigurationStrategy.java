// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.AdapterConfig;
import com.mercedesbenz.sechub.adapter.AdapterConfigurationStrategy;

public class NetworkTargetDataAdapterConfigurationStrategy implements AdapterConfigurationStrategy {

    private NetworkTargetType targetType;
    private NetworkTargetDataSuppport support;

    /**
     * Creates strategy which will automatically setup
     * 
     * <ol>
     * <li>trustAllCertificates</li>
     * <li>user</li>
     * <li>apiToken</li>
     * <li>password</li>
     * <li>productBaseUrl</li>
     * </ol>
     *
     * by information from given network target data provider  
     *
     * @param setup
     * @param targetType type to use to get data
     */
    public NetworkTargetDataAdapterConfigurationStrategy(NetworkTargetDataProvider dataProvider, NetworkTargetType targetType) {
        this.targetType = targetType;
        this.support = new NetworkTargetDataSuppport(dataProvider);
    }

    @Override
    public <B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> void configure(B configBuilder) {
        /* @formatter:off */
		configBuilder.
			setTrustAllCertificates(support.isHavingUntrustedCertificate(targetType)).
			setUser(support.getUserId(targetType)).
			setPasswordOrAPIToken(support.getPassword(targetType)).
			setProductBaseUrl(support.getBaseURL(targetType));
		/* @formatter:on */

    }

}
