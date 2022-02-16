// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.nessus;

import static org.junit.Assert.*;

import org.junit.Test;

import com.daimler.sechub.adapter.AbstractAdapterConfig;
import com.daimler.sechub.adapter.AbstractAdapterConfigBuilder;
import com.daimler.sechub.adapter.nessus.NessusConfig.NessusConfigBuilder;

/**
 * Name handling is tested here because its very important when using NETSPARKER
 * (each name produces costs)
 *
 * @author Albert Tregnaghi
 *
 */
public class NessusConfigBuilderTest {

    @Test
    public void configBuilder_is_child_of_abstract_adapter_config_builder() {
        assertTrue(AbstractAdapterConfigBuilder.class.isAssignableFrom(NessusConfigBuilder.class));
    }

    @Test
    public void config_is_child_of_abstract_adapter_config() {
        assertTrue(AbstractAdapterConfig.class.isAssignableFrom(NessusConfig.class));
    }

}
