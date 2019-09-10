// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.domain.scan.SecHubResult;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
import com.daimler.sechub.sharedkernel.util.JSONConverterException;
import com.daimler.sechub.sharedkernel.util.JSONable;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@MustBeKeptStable("This is the result returend from REST API to cli and other systems. So has to be stable")
public class ScanReportResult implements JSONable<ScanReportResult>{

	public static final String PROPERTY_RESULT="result";
	public static final String PROPERTY_JOBUUID="jobUUID";
	public static final String PROPERTY_TRAFFICLIGHT="trafficLight";
	public static final String PROPERTY_INFO="info";

	/**
	 * Just an reusable instance for JSON from calls - so we do not need to create
	 * always an empty object
	 */
	private static final SecHubResult SECHUB_RESULT = new SecHubResult();
	private static final Logger LOG = LoggerFactory.getLogger(ScanReportResult.class);

	UUID jobUUID;

	public UUID getJobUUID() {
		return jobUUID;
	}

	SecHubResult result;

	public SecHubResult getResult() {
		return result;
	}

	@JsonInclude(Include.NON_NULL) // only include info when additional data is set
	String info;

	public String getInfo() {
		return info;
	}

	String trafficLight;

	public String getTrafficLight() {
		return trafficLight;
	}

	public ScanReportResult(ScanReport report) {
		notNull(report, "Report may not be null!");
		jobUUID = report.getSecHubJobUUID();
		trafficLight = report.getTrafficLightAsString();
		try {
			result = SECHUB_RESULT.fromJSON(report.getResult());
		} catch (JSONConverterException e) {
			/* Should never happen, because we set secHubResult to database as string from report
			 * If this happens. We got a data corruption and are NOT backward compatible on changes.
			 * Must be then fixed!
			 */
			LOG.error("{} FATAL PROBLEM! Failed to create sechub result for origin:\n{}",UUIDTraceLogID.traceLogID(jobUUID), report.getResult());
			info="Origin result data problems! Please inform administrators about this problem.";
		}
	}

	@Override
	public Class<ScanReportResult> getJSONTargetClass() {
		return ScanReportResult.class;
	}
}
