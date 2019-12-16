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
public class TargetIdentifyingMultiInstallSetupConfigBuilderStrategy
		implements AdapterConfigurationStrategy{

	private TargetIdentifyingMultiInstallSetup setup;
	private TargetType targetType;

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
	 * by inspecting target type
	 *
	 * @param setup
	 * @param targetType type to use to get data
	 */
	public TargetIdentifyingMultiInstallSetupConfigBuilderStrategy(TargetIdentifyingMultiInstallSetup setup, TargetType targetType) {
		this.setup=setup;
		this.targetType=targetType;
	}

	@Override
	public <B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> void configure(B configBuilder) {
		/* @formatter:off */
		configBuilder.
			setTrustAllCertificates(setup.isHavingUntrustedCertificate(targetType)).
			setUser(setup.getUserId(targetType)).
			setPasswordOrAPIToken(setup.getPassword(targetType)).
			setProductBaseUrl(setup.getBaseURL(targetType));
		/* @formatter:on */

	}





}
