// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.report;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.UUIDTraceLogID;
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

	private static final Logger LOG = LoggerFactory.getLogger(ScanReportResult.class);
	
	private static final ScanReportResult IMPORTER = new ScanReportResult();

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
	
	private ScanReportResult() {
	    /* only internal for IMPORTER */
	}
	
	public ScanReportResult(ScanReport report) {
		notNull(report, "Report may not be null!");
		jobUUID = report.getSecHubJobUUID();
		trafficLight = report.getTrafficLightAsString();
		try {
			result = SecHubResult.fromJSONString(report.getResult());
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
	
	public static ScanReportResult fromJSONString(String json) {
	    return IMPORTER.fromJSON(json);
	}
	
}
