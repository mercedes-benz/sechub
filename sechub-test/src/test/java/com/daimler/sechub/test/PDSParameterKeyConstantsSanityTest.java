// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.adapter.pds.PDSAdapterConstants;
import com.daimler.sechub.pds.job.PDSJobConfigurationSupport;

class PDSParameterKeyConstantsSanityTest {

    @Test
    @DisplayName("PDS adapter and PDS itself must use exact same keys")
    void pds_and_sechub_do_use_same_key_constants_for_parameter_keys() {

        assertEquals(PDSAdapterConstants.PARAM_KEY_PRODUCT_IDENTIFIER, PDSJobConfigurationSupport.PARAM_KEY_PRODUCT_IDENTIFIER);
        assertEquals(PDSAdapterConstants.PARAM_KEY_SECHUB_STORAGE_PATH, PDSJobConfigurationSupport.PARAM_KEY_SECHUB_STORAGE_PATH);
        assertEquals(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE, PDSJobConfigurationSupport.PARAM_KEY_TARGET_TYPE);
        assertEquals(PDSAdapterConstants.PARAM_KEY_USE_SECHUB_STORAGE, PDSJobConfigurationSupport.PARAM_KEY_USE_SECHUB_STORAGE);

    }

}
