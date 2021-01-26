// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import org.springframework.scheduling.annotation.Scheduled;

import com.daimler.sechub.commons.core.util.SimpleStringUtils;
import com.daimler.sechub.docgen.spring.SpringValueExtractor.SpringValue;

public class SpringScheduleExtractor {
	public enum ScheduleType {
		UNDEFINED("Undefined"),

		CRON("Cron"),

		FIXED("Fixed"),

		;

		private String text;

		public String getText() {
			return text;
		}

		ScheduleType(String text) {
			this.text = text;
		}
	}

	private SpringValueExtractor valueExtractor;
	
	SpringValueExtractor getValueExtractor() {
		if (valueExtractor==null) {
			valueExtractor=new SpringValueExtractor();
		}
		return valueExtractor;
	}
	
	public class SpringSchedule {
		
		private String scheduleDefinition = "";
		private ScheduleType scheduleType;
		private String scheduleKey;
		private String scheduleDefaultValue;

		public String getScheduleDefaultValue() {
			return scheduleDefaultValue;
		}
		
		public String getScheduleDefinition() {
			return scheduleDefinition;
		}
		
		public String getScheduleKey() {
			return scheduleKey;
		}

		public ScheduleType getScheduleType() {
			if (scheduleType == null) {
				scheduleType = ScheduleType.UNDEFINED;
			}
			return scheduleType;
		}
	}

	public SpringSchedule extract(Scheduled springScheduled) {
		SpringSchedule data = extractSimpleString(springScheduled);
		if (getValueExtractor().isSpringValue(data.scheduleDefinition)) {
			SpringValue extracted = getValueExtractor().extract(data.scheduleDefinition);
			data.scheduleDefinition= extracted.toDescription();
			data.scheduleKey=extracted.getKey();
			data.scheduleDefaultValue=extracted.getDefaultValue();
		}
		return data;
	}
	
	private SpringSchedule extractSimpleString(Scheduled springScheduled) {
		SpringSchedule result = new SpringSchedule();
		if (springScheduled == null) {
			return result;
		}
		/* +-----------------------------------------------------------------------+ */
		/* +............................ CRON .....................................+ */
		/* +-----------------------------------------------------------------------+ */
		String cron = springScheduled.cron();
		if (SimpleStringUtils.isNotEmpty(cron)) {
			result.scheduleType = ScheduleType.CRON;
			result.scheduleDefinition = cron;
			return result;
		}
		/* +-----------------------------------------------------------------------+ */
		/* +............................ FIXED ...................................+ */
		/* +-----------------------------------------------------------------------+ */
		StringBuilder sb = new StringBuilder();
		appendInitialDelay(springScheduled, sb);
		appendFixedDelay(springScheduled, sb);
		appendFixedRate(springScheduled, sb);
		
		String fixed = sb.toString().trim();
		
		if (SimpleStringUtils.isEmpty(fixed)) {
			/* no fixed data available - so just return - type is UNDEFINED */
			result.scheduleType = ScheduleType.UNDEFINED;
			return result;
		}
		result.scheduleType = ScheduleType.FIXED;
		result.scheduleDefinition = fixed;
		return result;
	}

	private void appendFixedDelay(Scheduled springScheduled, StringBuilder sb) {
		String fixedDelayString = springScheduled.fixedDelayString();
		if (SimpleStringUtils.isEmpty(fixedDelayString)) {
			long fixedDelay = springScheduled.fixedDelay();
			if (fixedDelay != -1) {
				fixedDelayString = String.valueOf(fixedDelay);
			}
		}
		if (SimpleStringUtils.isNotEmpty(fixedDelayString)) {
			sb.append("fixed delay:").append(fixedDelayString).append(" ");
		}
	}
	private void appendFixedRate(Scheduled springScheduled, StringBuilder sb) {
		String fixedRateString = springScheduled.fixedRateString();
		if (SimpleStringUtils.isEmpty(fixedRateString)) {
			long fixedRate = springScheduled.fixedRate();
			if (fixedRate != -1) {
				fixedRateString = String.valueOf(fixedRate);
			}
		}
		if (SimpleStringUtils.isNotEmpty(fixedRateString)) {
			sb.append("fixed rate:").append(fixedRateString).append(" ");
		}
	}
	private void appendInitialDelay(Scheduled springScheduled, StringBuilder sb) {
		String initialDelayString = springScheduled.initialDelayString();
		if (SimpleStringUtils.isEmpty(initialDelayString)) {
			long initialDelay = springScheduled.initialDelay();
			if (initialDelay != -1) {
				initialDelayString = String.valueOf(initialDelay);
			}
		}
		if (SimpleStringUtils.isNotEmpty(initialDelayString)) {
			sb.append("initial delay:").append(initialDelayString).append(" ");
		}
	}
}
