// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import static com.mercedesbenz.sechub.commons.model.template.TemplateIdenifierConstants.*;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;

/**
 * Defines the template type
 *
 * @author Albert Tregnaghi
 *
 */
@MustBeKeptStable
public enum TemplateType {

    @JsonAlias({ ID_WEBSCAN_LOGIN })
    WEBSCAN_LOGIN(ID_WEBSCAN_LOGIN);

    private String id;

    TemplateType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
