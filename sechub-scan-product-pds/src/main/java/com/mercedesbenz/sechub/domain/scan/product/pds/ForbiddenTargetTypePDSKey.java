// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import com.mercedesbenz.sechub.commons.pds.AbstractPDSKey;
import com.mercedesbenz.sechub.domain.scan.TargetType;

public class ForbiddenTargetTypePDSKey extends AbstractPDSKey<SecHubProductExecutionPDSKey> implements SecHubProductExecutionPDSKey {

    private TargetType forbiddenTargetType;

    ForbiddenTargetTypePDSKey(String id, String description, TargetType forbiddenTargetType) {
        super(id, description);
        this.forbiddenTargetType = forbiddenTargetType;
    }

    public TargetType getForbiddenTargetType() {
        return forbiddenTargetType;
    }

}
