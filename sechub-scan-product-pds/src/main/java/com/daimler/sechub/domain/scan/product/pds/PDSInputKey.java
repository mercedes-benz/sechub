package com.daimler.sechub.domain.scan.product.pds;

public class PDSInputKey extends PDSSecHubConfigDataKey{

    PDSInputKey(String key, String description) {
        super(key, description);
    }

    @Override
    public boolean isReadFromSecHubExecutor() {
        return true;
    }

    @Override
    public boolean isAlwaysSentToPDS() {
        return false;
    }

}
