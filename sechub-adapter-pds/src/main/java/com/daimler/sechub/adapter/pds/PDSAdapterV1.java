// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.adapter.AbstractAdapter;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterProfiles;
import com.daimler.sechub.adapter.AdapterRuntimeContext;

/**
 * This component is able to handle PDS API V1
 *
 * @author Albert Tregnaghi
 *
 */
@Component
@Profile({ AdapterProfiles.REAL_PRODUCTS })
public class PDSAdapterV1 extends AbstractAdapter<PDSAdapterContext, PDSAdapterConfig> implements PDSAdapter {

    @Override
    public int getAdapterVersion() {
        return 1;
    }

    @Override
    protected String execute(PDSAdapterConfig config, AdapterRuntimeContext runtimeContext) throws AdapterException {
        PDSUrlBuilder urlBuilder = new PDSUrlBuilder(config.getProductBaseURL());

//        String url = urlBuilder.buildCreateJob();
        createNewPDSJOB(urlBuilder);
        throw asAdapterException("pds adapter needs implementation...", config);
    }
    
    private UUID createNewPDSJOB(PDSUrlBuilder builder) {
        return null;
    }
    

    @Override
    protected String getAPIPrefix() {
        return "/api/";
    }
}
