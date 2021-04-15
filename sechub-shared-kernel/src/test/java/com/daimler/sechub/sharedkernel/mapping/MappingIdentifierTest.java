// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.mapping;

import static com.daimler.sechub.sharedkernel.mapping.MappingIdentifier.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier.MappingType;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class MappingIdentifierTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    
    @Test
    public void identifiers_are_constructed_without_initialization_failures() {
        /* we just aqccess mappping identifier - if some body has implemented wrong (e.g. duplicates or wrong ids)
         * this will throw an exception at construction time.
         */
        assertNotNull(MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId());
        
    }
    
    @Test
    public void assertNoDuplicate_works() {
        /* test */
        expected.expect(IllegalStateException.class);
        expected.expectMessage("duplicate detected");
        expected.expectMessage(CHECKMARX_NEWPROJECT_PRESET_ID.getId());
        
        /* execute */
        assertNoDuplicate(CHECKMARX_NEWPROJECT_PRESET_ID.getId());
    }
    
    @Test
    public void assertValidId_works() {
        /* test */
        expected.expect(IllegalStateException.class);
        expected.expectMessage("not valid");
        
        /* execute */
        assertValidId(CHECKMARX_NEWPROJECT_PRESET_ID.getId()+" ");
    }
    
    
    @Test
    public void getIdentifierOrNull_returns_correct_identifier() {
        /* just test two known parts are returned as expected:*/
        assertEquals(CHECKMARX_NEWPROJECT_TEAM_ID, getIdentifierOrNull(CHECKMARX_NEWPROJECT_TEAM_ID.getId()));
        assertEquals(CHECKMARX_NEWPROJECT_PRESET_ID, getIdentifierOrNull(CHECKMARX_NEWPROJECT_PRESET_ID.getId()));
    }
    
    @Test
    public void getIdentifierOrNull_returns_null_for_unknown_id() {
        /* just test two known parts are returned as expected:*/
        assertNull(getIdentifierOrNull("1234-1234-1234-unknown"));
    }
    
    @Test
    public void getIdentifierOrNull_returns_null_for_null() {
        /* just test two known parts are returned as expected:*/
        assertNull(getIdentifierOrNull(null));
    }
    
    @Test
    public void CHECKMARX_NEW_PROJECT_TEAM_ID_is_only_a_product_executor_parameter_template_mapping() {
        assertEquals(MappingType.PRODUCT_EXECUTOR_CONFIGURATION_PARAMETER, CHECKMARX_NEWPROJECT_TEAM_ID.getType());
    }
    
    @Test
    public void hasTypeContainedIn_works() {
        MappingIdentifier adapterConfigId = CHECKMARX_NEWPROJECT_TEAM_ID;
        
        assertTrue(adapterConfigId.hasTypeContainedIn(MappingType.PRODUCT_EXECUTOR_CONFIGURATION_PARAMETER));
        assertTrue(adapterConfigId.hasTypeContainedIn(MappingType.COMMON_CONFIGURATION,MappingType.PRODUCT_EXECUTOR_CONFIGURATION_PARAMETER));
        assertTrue(adapterConfigId.hasTypeContainedIn(MappingType.PRODUCT_EXECUTOR_CONFIGURATION_PARAMETER,MappingType.COMMON_CONFIGURATION));
        assertTrue(adapterConfigId.hasTypeContainedIn(MappingType.PRODUCT_EXECUTOR_CONFIGURATION_PARAMETER,MappingType.ADAPTER_CONFIGURATION));
        
        assertFalse(adapterConfigId.hasTypeContainedIn(MappingType.COMMON_CONFIGURATION));
        assertFalse(adapterConfigId.hasTypeContainedIn(MappingType.ADAPTER_CONFIGURATION));
        assertFalse(adapterConfigId.hasTypeContainedIn((MappingType[])null));
        assertFalse(adapterConfigId.hasTypeContainedIn(new MappingType[] {}));
        assertFalse(adapterConfigId.hasTypeContainedIn(new MappingType[] {null}));
    }
    
    
    

}
