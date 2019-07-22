// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import com.daimler.sechub.sharedkernel.TraceLogID;

public class DomainDataTraceLogID extends TraceLogID<DomainMessagePart> {

	public DomainDataTraceLogID(DomainMessagePart source) {
		super(source);
	}

	public static DomainDataTraceLogID traceLogID(DomainMessagePart data) {
		return new DomainDataTraceLogID(data);
	}

	@Override
	protected String createContent(DomainMessagePart source) throws Exception {
		if (source==null) {
			return null;
		}
		return source.getRaw(MessageDataKeys.SECHUB_UUID.getId());
	}

}
