// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.execution;

import java.util.UUID;

import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;

/**
 * Execution context with scope of SecHub, means knows {@link SecHubJob} ID and
 * {@link SecHubConfiguration}
 *
 * @author Albert Tregnaghi
 *
 */
public class SecHubExecutionContext {

	private UUID sechubJobUUID;
	private SecHubConfiguration configuration;
	private UUIDTraceLogID traceLogId;
	private String executedBy;

	public SecHubExecutionContext(UUID sechubJobUUID, SecHubConfiguration configuration, String executedBy) {
		this.sechubJobUUID = sechubJobUUID;
		this.configuration = configuration;
		this.executedBy=executedBy;
		this.traceLogId=UUIDTraceLogID.traceLogID(sechubJobUUID);
	}

	public String getExecutedBy() {
		return executedBy;
	}

	public UUID getSechubJobUUID() {
		return sechubJobUUID;
	}

	public SecHubConfiguration getConfiguration() {
		return configuration;
	}

	public UUIDTraceLogID getTraceLogId() {
		return traceLogId;
	}

	public String getTraceLogIdAsString() {
		return traceLogId.toString();
	}

}
