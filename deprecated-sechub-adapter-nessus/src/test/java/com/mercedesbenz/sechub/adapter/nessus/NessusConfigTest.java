// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mercedesbenz.sechub.adapter.AbstractAdapterConfig;
import com.mercedesbenz.sechub.adapter.AbstractAdapterConfigBuilder;
import com.mercedesbenz.sechub.adapter.nessus.NessusConfig.NessusConfigBuilder;

public class NessusConfigTest {
    @Test
    public void configBuilder_is_child_of_abstract_adapter_config_builder() {
        assertTrue(AbstractAdapterConfigBuilder.class.isAssignableFrom(NessusConfigBuilder.class));
    }

    @Test
    public void config_is_child_of_abstract_adapter_config() {
        assertTrue(AbstractAdapterConfig.class.isAssignableFrom(NessusConfig.class));
    }

}
