// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import com.mercedesbenz.sechub.commons.core.ConfigurationFailureException;

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
    <B extends AdapterConfigBuilder, C extends AdapterConfig> void configure(B configBuilder) throws ConfigurationFailureException;

}
