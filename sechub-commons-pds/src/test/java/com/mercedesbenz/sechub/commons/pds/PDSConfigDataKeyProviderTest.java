// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PDSConfigDataKeyProviderTest {

    @Test
    void target_type_is_generated() {
        assertTrue(PDSConfigDataKeyProvider.PDS_SCAN_TARGET_TYPE.getKey().isGenerated());
    }

    @Test
    void use_sechub_storage_is_always_sent_to_pds() {
        assertTrue(PDSConfigDataKeyProvider.PDS_CONFIG_USE_SECHUB_STORAGE.getKey().isSentToPDS());
    }

    @Test
    void sechub_storage_path_is_generated() {
        assertTrue(PDSConfigDataKeyProvider.PDS_CONFIG_SECHUB_STORAGE_PATH.getKey().isGenerated());
    }

    @Test
    void sechub_filefilter_includes_is_sent_to_pds() {
        assertTrue(PDSConfigDataKeyProvider.PDS_CONFIG_FILEFILTER_INCLUDES.getKey().isSentToPDS());
    }

    @Test
    void sechub_filefilter_excludes_is_sent_to_pds() {
        assertTrue(PDSConfigDataKeyProvider.PDS_CONFIG_FILEFILTER_EXCLUDES.getKey().isSentToPDS());
    }

}
