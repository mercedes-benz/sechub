// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.mapping;

import static java.util.Objects.*;

import java.util.Map;
import java.util.TreeMap;

import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.validation.MappingIdValidationImpl;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

/**
 * Represents all mapping identifiers.
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable("The enum is used as identifiers in database + in code. Do NOT rename Ids inside")
public enum MappingIdentifier {
   
    CHECKMARX_NEWPROJECT_TEAM_ID("checkmarx.newproject.teamid", MappingType.ADAPTER_CONFIGURATION),

    CHECKMARX_NEWPROJECT_PRESET_ID("checkmarx.newproject.presetid",MappingType.ADAPTER_CONFIGURATION),
    
    ;

    private String id;
    private MappingType type;

    private MappingIdentifier(String id, MappingType type) {
        requireNonNull(type);
        assertValidId(id);
        assertNoDuplicate(id);
        
        this.id = id;
        this.type=type;
        StaticRef.map.put(id, this);
    }

    static void assertNoDuplicate(String id) {
        MappingIdentifier identifierFound = getIdentifierOrNull(id);
        if (identifierFound!=null) {
            throw new IllegalStateException("Implementation failure, duplicate detected! Mapping identifier:"+identifierFound+" has already id:"+id);
        }
    }

    static void assertValidId(String id) {
        ValidationResult result = StaticRef.mappingIdValidation.validate(id);
        /* ensure enumeration ids are always valid */
        if (!result.isValid()) {
            throw new IllegalStateException("Implementation failure, mapping id not valid:"+result.getErrorDescription());
        }
    }

    public String getId() {
        return id;
    }
    
    public MappingType getType() {
        return type;
    }
    
    
    /**
     * Get identifier by given id
     * @param id
     * @return identifier or <code>null</code> when not found
     */
    public static MappingIdentifier getIdentifierOrNull(String id) {
        if (id==null) {
            return null;
        }
        return StaticRef.map.get(id);
    }
    
    public static enum MappingType{
        GLOBAL_CONFIGURATION,
        ADAPTER_CONFIGURATION
    }
    
    /**
     * Check if this mapping identifier has a type which is contained in one of the given ones
     * @param acceptedTypes
     * @return <code>true</code> when this identifier has one of given types
     */
    public boolean hasTypeContainedIn(MappingType ...acceptedTypes) {
        if (acceptedTypes==null) {
            return false;
        }
        /* filter only relevant parts - message may contain uninteresting stuff */
        boolean relevant = false;
        for (MappingType type: acceptedTypes) {
            relevant = type == getType();
            if (relevant) {
                break;
            }
        }
        return relevant;
    }
    
    private static class StaticRef{
        /* Constructor of enumeration cannot have access to static parts of enum - so this workaround: */
        private static final MappingIdValidationImpl mappingIdValidation = new MappingIdValidationImpl();
        private static final Map<String, MappingIdentifier> map = new TreeMap<>();
    }
}
