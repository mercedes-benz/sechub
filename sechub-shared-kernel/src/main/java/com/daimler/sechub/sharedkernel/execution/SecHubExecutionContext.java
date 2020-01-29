// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.sharedkernel.TypedKey;
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

	private static final Logger LOG = LoggerFactory.getLogger(SecHubExecutionContext.class);

	private UUID sechubJobUUID;
	private SecHubConfiguration configuration;
	private UUIDTraceLogID traceLogId;
	private String executedBy;
	private Map<String, Object> dataMap = new HashMap<>();

	public SecHubExecutionContext(UUID sechubJobUUID, SecHubConfiguration configuration, String executedBy) {
		this.sechubJobUUID = sechubJobUUID;
		this.configuration = configuration;
		this.executedBy = executedBy;
		this.traceLogId = UUIDTraceLogID.traceLogID(sechubJobUUID);
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

	/**
	 * Add additional data by typed key
	 * @param <V>
	 * @param id
	 * @param value
	 */
	public <V> void putData(TypedKey<V> id, V value) {
		if (id==null) {
			return;
		}
		dataMap.put(id.getId(), value);
	}

	/**
	 * Get additional data by typed key
	 * @param <V>
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <V> V getData(TypedKey<V> id) {
		if (id==null) {
			return null;
		}
		Object value = dataMap.get(id.getId());
		if (value == null) {
			return null;
		}
		if (id.getValueClass().isAssignableFrom(value.getClass())) {
			return (V) value;
		}
		LOG.error("Wrong usage in code: found entry for key '{}', but type '{}' found instead of wanted '{}'",id.getId(),value.getClass(),id.getValueClass());
		return null;
	}

}
