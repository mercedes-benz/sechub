// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PDSConfigDataKeyProviderTest {

    @Test
    void target_type_is_generated() {
        assertTrue(PDSConfigDataKeyProvider.PDS_TARGET_TYPE.getKey().isGenerated());
    }
    
    @Test
    void use_sechub_storage_is_always_sent_to_pds() {
        assertTrue(PDSConfigDataKeyProvider.PDS_USE_SECHUB_STORAGE.getKey().isSentToPDS());
    }
    
    @Test
    void sechub_storage_path_is_generated() {
        assertTrue(PDSConfigDataKeyProvider.PDS_SECHUB_STORAGE_PATH.getKey().isGenerated());
    }

}
