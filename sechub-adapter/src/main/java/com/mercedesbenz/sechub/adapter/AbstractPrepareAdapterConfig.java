package com.mercedesbenz.sechub.adapter;

public abstract class AbstractPrepareAdapterConfig extends AbstractAdapterConfig implements PrepareAdapterConfig {
    @Override
    public String getTargetAsString() {
        return null;
        // TODO ?
        // for prepare we cannot define a target as a string
    }
}
