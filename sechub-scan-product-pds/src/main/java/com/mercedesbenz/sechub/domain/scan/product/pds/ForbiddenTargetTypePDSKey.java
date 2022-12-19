// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.pds;

import com.mercedesbenz.sechub.commons.pds.AbstractPDSKey;
import com.mercedesbenz.sechub.domain.scan.NetworkTargetType;

public class ForbiddenTargetTypePDSKey extends AbstractPDSKey<SecHubProductExecutionPDSKey> implements SecHubProductExecutionPDSKey {

    private NetworkTargetType forbiddenTargetType;

    ForbiddenTargetTypePDSKey(String id, String description, NetworkTargetType forbiddenTargetType) {
        super(id, description);
        this.forbiddenTargetType = forbiddenTargetType;
    }

    public NetworkTargetType getForbiddenTargetType() {
        return forbiddenTargetType;
    }

}
