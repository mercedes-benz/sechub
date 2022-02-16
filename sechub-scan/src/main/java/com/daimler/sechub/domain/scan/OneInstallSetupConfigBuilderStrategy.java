// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.AdapterConfig;
import com.daimler.sechub.adapter.AdapterConfigurationStrategy;

/**
 * A common strategy for one install setup configuration - usable by every
 * product executor to configure corresponding adapter easily by sechub
 * configuration. <br>
 * <br>
 * Using this strategy will reduce boilerplate code in web scan executors.
 *
 * @author Albert Tregnaghi
 *
 * @param <B> builder
 * @param <C> configuration
 */
public class OneInstallSetupConfigBuilderStrategy implements AdapterConfigurationStrategy {

    private OneInstallSetup setup;

    /**
     * Creates strategy which will automatically setup
     * <ol>
     * <li>trustAllCertificates</li>
     * <li>user</li>
     * <li>apiToken</li>
     * <li>password</li>
     * <li>productBaseUrl</li>
     * </ol>
     *
     * @param setup
     */
    public OneInstallSetupConfigBuilderStrategy(OneInstallSetup setup) {
        this.setup = setup;
    }

    @Override
    public <B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> void configure(B configBuilder) {
        /* @formatter:off */
		configBuilder.
			setTrustAllCertificates(setup.isHavingUntrustedCertificate()).
			setUser(setup.getUserId()).
			setPasswordOrAPIToken(setup.getPassword()).
			setProductBaseUrl(setup.getBaseURL());
		/* @formatter:on */

    }

}
