// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.template.TemplateType;

@MustBeKeptStable("You can rename enums, but do not change id parts, because used inside DB!")
public enum ScanProjectConfigID {

    /**
     * Id to fetch mock configuration data, which will contain JSON representing a
     * ScanProjectMockDataConfiguration object
     */
    MOCK_CONFIGURATION("mock_config"),

    /**
     * Id to fetch false positive configuration data, which will contain JSON
     * representing a FalsePositiveProjectConfiguration object
     */
    FALSE_POSITIVE_CONFIGURATION("false_positives"),

    /**
     * Id to fetch project access level data, which will contain just the id of the
     * access level.
     */
    PROJECT_ACCESS_LEVEL("project_access_level"),

    TEMPLATE_WEBSCAN_LOGIN("template_" + TemplateType.WEBSCAN_LOGIN.name().toLowerCase()),

    ;

    private String id;

    ScanProjectConfigID(String id) {
        notNull(id, "config id may not be null!");
        maxLength(id, 60); // in DB we got 3x20 defined, but we have only ascii chars allowed, so 60 is max
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
