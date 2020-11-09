package com.daimler.sechub.domain.scan.product.pds;

import com.daimler.sechub.domain.scan.TargetType;

public class PDSForbiddenTargetTypeInputKey extends PDSProductExecutorKey{

    private TargetType forbiddenTargetType;
    
    PDSForbiddenTargetTypeInputKey(String key, String description, TargetType forbiddenTargetType) {
        super(key, description);
        this.forbiddenTargetType=forbiddenTargetType;
    }
    
    public TargetType getForbiddenTargetType() {
        return forbiddenTargetType;
    }

}
