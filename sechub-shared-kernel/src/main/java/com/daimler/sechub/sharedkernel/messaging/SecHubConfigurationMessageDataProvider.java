// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.commons.model.SecHubRuntimeException;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;

public class SecHubConfigurationMessageDataProvider implements MessageDataProvider<SecHubConfiguration>{

	private static final SecHubConfiguration OBJECT = new SecHubConfiguration();

	@Override
	public SecHubConfiguration get(String data) {
		if (data==null) {
			return null;
		}
		try {
			return OBJECT.fromJSON(data);
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
