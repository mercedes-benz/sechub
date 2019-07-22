// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

public class StringMessageDataProvider implements MessageDataProvider<String>{

	@Override
	public String get(String data) {
		if (data==null) {
			return null;
		}
		return data;
	}

	@Override
	public String getString(String content) {
		return content;
	}

}
