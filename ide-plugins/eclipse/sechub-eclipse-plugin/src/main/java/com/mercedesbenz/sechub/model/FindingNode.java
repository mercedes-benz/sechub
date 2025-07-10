// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.api.internal.gen.model.Severity;

/**
 * The FindingNode represents data structure required in SecHubReportView
 */
public class FindingNode implements Comparable<FindingNode> {

	private FindingNode parent = null;
	private List<FindingNode> children = new LinkedList<FindingNode>();

	private String description;
	private String location;
	private Integer line;
	private Integer column;
	private String relevantPart;
	private String source;
	public int callStackStep;
	private String fileName;
	public String filePath;
	private Map<String, Object> metaDataCache;
	public SecHubFinding finding;

	private FindingNode(String description, String location, Integer line, Integer column, String relevantPart,
			String source) {
		this.description = description;
		this.location = location;
		this.line = line;
		this.column = column;
		this.relevantPart = relevantPart;
		this.source = source;
	}

	public static class FindingNodeBuilder {
		private String description;
		private String location;
		private int line;
		private int column;
		private int callStackStep;
		private String relevantPart;
		private String source;
		private SecHubFinding finding;

		private FindingNodeBuilder() {

		}

		public FindingNodeBuilder setDescription(String description) {
			this.description = description;
			return this;
		}

		public FindingNodeBuilder setLocation(String location) {
			this.location = location;
			return this;
		}

		public FindingNodeBuilder setLine(int line) {
			this.line = line;
			return this;
		}

		public FindingNodeBuilder setColumn(int column) {
			this.column = column;
			return this;
		}

		public FindingNodeBuilder setCallStackStep(int callStackStep) {
			this.callStackStep = callStackStep;
			return this;
		}

		public FindingNodeBuilder setFinding(SecHubFinding finding) {
			this.finding = finding;
			return this;
		}

		public FindingNodeBuilder setRelevantPart(String relevantPart) {
			this.relevantPart = relevantPart;
			return this;
		}

		public FindingNodeBuilder setSource(String source) {
			this.source = source;
			return this;
		}

		public FindingNode build() {
			FindingNode node = new FindingNode(description, location, line, column, relevantPart, source);
			node.callStackStep = callStackStep;
			node.finding = finding;

			calculateFileNameAndPath(node);

			return node;
		}

		private void calculateFileNameAndPath(FindingNode node) {
			String location = node.getLocation();
			if (location == null) {
				return;
			}
			int lastIndex = location.lastIndexOf('/');
			if (lastIndex == -1) {
				node.fileName = location;
				node.filePath = "";
				return;
			}
			node.filePath = location.substring(0, lastIndex);
			if (lastIndex >= location.length()) {
				node.fileName = "";
			} else {
				node.fileName = location.substring(lastIndex + 1);
			}
		}

	}

	public static FindingNodeBuilder builder() {
		return new FindingNodeBuilder();
	}

	public FindingNode getParent() {
		return parent;
	}

	public boolean addChild(FindingNode finding) {
		if (this == finding) {
			throw new IllegalStateException("finding cannot be parent of itself!");
		}
		finding.parent = this;

		return children.add(finding);
	}

	public List<FindingNode> getChildren() {
		return this.children;
	}

	public boolean hasChildren() {
		boolean hasChildren = false;

		if (this.children.size() > 0) {
			hasChildren = true;
		}

		return hasChildren;
	}

	public String getDescription() {
		return description;
	}

	public String getLocation() {
		return location;
	}

	public Integer getLine() {
		return line;
	}

	public Integer getColumn() {
		return column;
	}

	public String getRelevantPart() {
		return relevantPart;
	}

	public String getSource() {
		return source;
	}

	public Severity getSeverity() {
		return finding.getSeverity();
	}

	public int getId() {
		return finding.getId();
	}

	public SecHubFinding getFinding() {
		return finding;
	}

	public Integer getCweId() {
		return finding.getCweId();
	}

	public int getCallStackStep() {
		return callStackStep;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setCachedMetaData(String key, Object value) {
		synchronized (monitor) {
			if (metaDataCache == null) {
				metaDataCache = new HashMap<>();
			}
			metaDataCache.put(key, value);
		}
	}

	public Object getCachedMetaData(String key) {
		synchronized (monitor) {
			if (metaDataCache == null) {
				metaDataCache = new HashMap<>();
			}
			return metaDataCache.get(key);
		}
	}

	private Object monitor = new Object();

	@Override
	public String toString() {
		return "FindingNode [parent=" + parent + ", children=" + children + ", description=" + description
				+ ", location=" + location + ", line=" + line + ", column=" + column + ", relevantPart=" + relevantPart
				+ ", source=" + source + ", callStackStep=" + callStackStep + ", finding=" + finding + "]";
	}

	@Override
	public int compareTo(FindingNode other) {
		if (other == null || other.finding == null) {
			return 1;
		}

		if (this.finding == null) {
			return -1;
		}

		Integer thisId = this.finding.getId();
		Integer otherId = other.finding.getId();

		if (thisId == null && otherId == null) {
			return 0;
		} else if (thisId == null) {
			return -1;
		} else if (otherId == null) {
			return 1;
		} else {
			return thisId.compareTo(otherId);
		}
	}

}
