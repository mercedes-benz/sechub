// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

/**
 * Identifies an adapter - e.g. for logging and exceptions. So we do not
 * need dedicated exceptions for every adapter implementation...
 * @author Albert Tregnaghi
 *
 */
public class AdapterLogId {

	private String id;
	private String traceId;

	public AdapterLogId(String id, String traceId) {
		this.id=id;
		this.traceId=traceId;
	}
	
	public String getId() {
		return id;
	}
	
	public String getTraceId() {
		return traceId;
	}

	public String withMessage(String message) {
		StringBuilder sb = new StringBuilder();
		if (traceId!=null) {
			sb.append(traceId);
			sb.append(" ");
		}
		sb.append(id);
		if (message!=null) {
			sb.append(":");
			sb.append(message);
		}
		return sb.toString();
	}
	
	public String toString() {
		return withMessage(null);
	}
}
