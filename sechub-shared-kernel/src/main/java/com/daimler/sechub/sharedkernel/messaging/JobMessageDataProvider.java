// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.sharedkernel.util.JSONConverterException;
import com.daimler.sechub.sharedkernel.util.SecHubRuntimeException;

public class JobMessageDataProvider implements MessageDataProvider<JobMessage>{

	@Override
	public JobMessage get(String data) {
		if (data==null) {
			return null;
		}
		try {
			return JobMessage.OBJECT.fromJSON(data);
		} catch (JSONConverterException e) {
			throw new SecHubRuntimeException("Cannot convert", e);
		}

	}

	@Override
	public String getString(JobMessage message) {
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
