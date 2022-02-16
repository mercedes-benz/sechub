// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

/**
 * A configuration strategy is used to configure a given config adapter builder
 *
 * @author Albert Tregnaghi
 *
 * @param <B>
 * @param <C>
 */
public interface AdapterConfigurationStrategy {

    /**
     * Configures the given config builder
     *
     * @param configBuilder
     */
    <B extends AbstractAdapterConfigBuilder<B, C>, C extends AdapterConfig> void configure(B configBuilder);

}
