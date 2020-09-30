// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.commons.model.SecHubRuntimeException;

public class MappingMessageDataProvider implements MessageDataProvider<MappingMessage>{

	private static final MappingMessage OBJECT = new MappingMessage();

	@Override
	public MappingMessage get(String data) {
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
	public String getString(MappingMessage message) {
		if (message==null) {
			return null;
		}
		try {
			return message.toJSON();
		} catch (JSONConverterException e) {
			throw new SecHubRuntimeException("Cannot convert", e);
		}
	}


}
