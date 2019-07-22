// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.util.JSONConverterException;
import com.daimler.sechub.sharedkernel.util.SecHubRuntimeException;

public class SecHubConfigurationMessageDataProvider implements MessageDataProvider<SecHubConfiguration>{

	@Override
	public SecHubConfiguration get(String data) {
		if (data==null) {
			return null;
		}
		try {
			return SecHubConfiguration.OBJECT.fromJSON(data);
		} catch (JSONConverterException e) {
			throw new SecHubRuntimeException("Cannot convert", e);
		}
		
	}

	@Override
	public String getString(SecHubConfiguration configuration) {
		if (configuration==null) {
			return null;
		}
		try {
			return configuration.toJSON();
		} catch (JSONConverterException e) {
			throw new SecHubRuntimeException("Cannot convert", e);
		}
	}


}
