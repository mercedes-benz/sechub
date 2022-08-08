package com.mercedesbenz.sechub.adapter.pds;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;

@Component
class PDSContextFactoryImpl implements PDSContextFactory {

    @Override
    public PDSContext create(PDSAdapterConfig config, PDSAdapter pdsAdapter, AdapterRuntimeContext runtimeContext) {
        return new PDSContext(config, pdsAdapter, runtimeContext);
    }
}