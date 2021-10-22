// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.commons.pds.AbstractPDSKey;
import com.daimler.sechub.domain.scan.TargetType;

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
