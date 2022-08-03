package com.mercedesbenz.sechub.test.mapping;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;

class CrossLibraryMappingTest {

    @Test
    void checkmarx_mappings_in_sharedkernel_and_adapter_use_same_ids() {
        assertEquals(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID, MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId());
        assertEquals(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID, MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId());
    }

}
