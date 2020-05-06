// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.util.Map;

import com.daimler.sechub.adapter.Adapter;
import com.daimler.sechub.adapter.AdapterCanceledByUserException;
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterLogId;
import com.daimler.sechub.adapter.AdapterMetaDataCallback;
import com.daimler.sechub.adapter.TraceIdProvider;

public class IntegrationTestAdapter implements Adapter<IntegrationTestAdapterConfig>{


	@Override
	public AdapterLogId getAdapterLogId(TraceIdProvider provider) {
		return new AdapterLogId("testadapter",provider.getTraceID());
	}

	@Override
	public AdapterCanceledByUserException asAdapterCanceledByUserException(TraceIdProvider provider) {
		throw new IllegalStateException("should not happen");
	}

	@Override
	public AdapterException asAdapterException(String message, TraceIdProvider provider) {
		return new AdapterException(getAdapterLogId(provider), message);
	}

	@Override
	public AdapterException asAdapterException(String message, Throwable t, TraceIdProvider provider) {
		return new AdapterException(getAdapterLogId(provider), message,t);
	}

	@Override
	public String createAPIURL(String apiPart, IntegrationTestAdapterConfig config) {
		return createAPIURL(apiPart, config);
	}

	@Override
	public String createAPIURL(String apiPart, IntegrationTestAdapterConfig config, Map<String, String> map) {
		return "http://testadapter/"+apiPart+"?"+map;
	}

	
	@Override
	public int getAdapterVersion() {
		return 666;
	}

    @Override
    public String start(IntegrationTestAdapterConfig config, AdapterMetaDataCallback callback) throws AdapterException {
        return null;
    }

    @Override
    public boolean stop(IntegrationTestAdapterConfig config, AdapterMetaDataCallback callback) throws AdapterException {
        return false;
    }

}
