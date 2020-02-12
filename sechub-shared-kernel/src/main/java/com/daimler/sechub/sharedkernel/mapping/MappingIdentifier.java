package com.daimler.sechub.sharedkernel.mapping;

import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.validation.MappingIdValidationImpl;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

/**
 * Contains all wellknown mapping identifiers. Will also ensure we have no duplicates.
 * This is one of the reasons why this is inside shared kernel!
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable("The enum id parts are used as identifiers in database + in code. Do NOT rename identifiers inside or remove values. Mark older products as deprecated!")
public enum MappingIdentifier {
   
    CHECKMARX_NEWPROJECT_TEAM_ID("checkmarx.newproject.teamid"),

    CHECKMARX_NEWPROJECT_PRESET_ID("checkmarx.newproject.presetid"),

    ;

    private MappingIdValidationImpl mappingIdValidation = new MappingIdValidationImpl();
    private String id;

    private MappingIdentifier(String id) {
        ValidationResult result = mappingIdValidation.validate(id);
        /* ensure enumeration ids are always valid */
        if (!result.isValid()) {
            throw new IllegalStateException("Code failure:"+result.getErrorDescription());
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
