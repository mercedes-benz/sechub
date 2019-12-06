// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.daimler.sechub.docgen.DocAnnotationData;
import com.daimler.sechub.docgen.Generator;
import com.daimler.sechub.docgen.spring.SpringScheduleExtractor.SpringSchedule;
import com.daimler.sechub.docgen.util.AnnotationDataLocationExtractor;
import com.daimler.sechub.docgen.util.ClasspathDataCollector;

public class ScheduleDescriptionGenerator implements Generator{

	SpringScheduleExtractor springScheduledExtractor;
	AnnotationDataLocationExtractor locationExtractor;

	public ScheduleDescriptionGenerator() {
		this.springScheduledExtractor = new SpringScheduleExtractor();
		this.locationExtractor=new AnnotationDataLocationExtractor();
	}

	public String generate(ClasspathDataCollector collector) {
		if (collector == null) {
			return "";
		}
		List<DocAnnotationData> list = collector.fetchMustBeDocumentParts();
		if (list == null || list.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Map<String, SortedSet<TableRow>> rowMap = new TreeMap<>();
		for (DocAnnotationData data : list) {
			if (data.springScheduled == null) {
				continue;
			}
			SpringSchedule extracted = springScheduledExtractor.extract(data.springScheduled);
			TableRow row = new TableRow();
			row.scheduleType = extracted.getScheduleType().getText();
			row.scheduleDefinition = extracted.getScheduleDefinition();
			row.description = data.description;
			row.location = locationExtractor.extractLocation(data);

			SortedSet<TableRow> rows = rowMap.get(data.scope);
			if (rows == null) {
				rows = new TreeSet<>();
				rowMap.put(data.scope, rows);
			}
			rows.add(row);
		}
		if (rowMap.isEmpty()) {
			return "";
		}
		for (Map.Entry<String, SortedSet<TableRow>> entries : rowMap.entrySet()) {
			SortedSet<TableRow> table = entries.getValue();

			sb.append("[options=\"header\",cols=\"1,1,1,1\"]\n");
			sb.append(".").append(buildTitle(entries.getKey()));
			sb.append("\n|===\n");
			sb.append("|Type   |Definition   |Description\n");
			sb.append("//----------------------\n");
			for (TableRow row : table) {
				sb.append("|").append(row.scheduleType);
				sb.append("|").append(row.scheduleDefinition);
				sb.append("|").append(row.description);
				sb.append("\n");
			}
			sb.append("\n|===\n\n");
		}
		return sb.toString();
	}

	private String buildTitle(String key) {
		return "Scope '" + key + "'";
	}

	private class TableRow implements Comparable<TableRow> {
		String scheduleDefinition;
		String scheduleType;
		String description;
		String location;

		@Override
		public int compareTo(TableRow o) {
			return getScheduleDefinition().compareTo(o.getScheduleDefinition());
		}

		public String getScheduleDefinition() {
			if (scheduleDefinition == null) {
				return "";
			}
			return scheduleDefinition;
		}

		private ScheduleDescriptionGenerator getOuterType() {
			return ScheduleDescriptionGenerator.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((description == null) ? 0 : description.hashCode());
			result = prime * result + ((location == null) ? 0 : location.hashCode());
			result = prime * result + ((scheduleDefinition == null) ? 0 : scheduleDefinition.hashCode());
			result = prime * result + ((scheduleType == null) ? 0 : scheduleType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TableRow other = (TableRow) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (description == null) {
				if (other.description != null)
					return false;
			} else if (!description.equals(other.description))
				return false;
			if (location == null) {
				if (other.location != null)
					return false;
			} else if (!location.equals(other.location))
				return false;
			if (scheduleDefinition == null) {
				if (other.scheduleDefinition != null)
					return false;
			} else if (!scheduleDefinition.equals(other.scheduleDefinition))
				return false;
			if (scheduleType == null) {
				if (other.scheduleType != null)
					return false;
			} else if (!scheduleType.equals(other.scheduleType))
				return false;
			return true;
		}
	}
}
