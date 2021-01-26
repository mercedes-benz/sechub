// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.domain.scan.TargetType;

public class PDSForbiddenTargetTypeInputKey extends PDSProductExecutorKey {

    private TargetType forbiddenTargetType;

    PDSForbiddenTargetTypeInputKey(String id, String description, TargetType forbiddenTargetType) {
        super(id, description);
        this.forbiddenTargetType = forbiddenTargetType;
    }

    public TargetType getForbiddenTargetType() {
        return forbiddenTargetType;
    }

}
