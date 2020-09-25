// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.commons.model.SecHubRuntimeException;

public class SchedulerMessageDataProvider implements MessageDataProvider<SchedulerMessage>{

	private static final SchedulerMessage OBJECT = new SchedulerMessage();

	@Override
	public SchedulerMessage get(String data) {
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
	public String getString(SchedulerMessage message) {
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
