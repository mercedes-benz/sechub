// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.commons.model.Severity;

/**
 * The FindingNode represents data structure required in
 * SecHubReportView
 */
public class FindingNode implements Comparable<FindingNode> {

	private FindingNode parent = null;
	private List<FindingNode> children = new LinkedList<FindingNode>();

	private Integer cweId;
	private String description;
	private String location;
	private Integer line;
	private Integer column;
	private String relevantPart;
	private String source;
	private Severity severity;
	public int callStackStep;
	public int id;
	private String fileName;
	public String filePath;
	private Map<String,Object> metaDataCache;

	private FindingNode(String description, String location, Integer line, Integer column, String relevantPart,
			String source, Severity severity) {
		this.description = description;
		this.location = location;
		this.line = line;
		this.column = column;
		this.relevantPart = relevantPart;
		this.source = source;
		this.severity = severity;
	}

	public static class FindingNodeBuilder {
		private String description;
		private String location;
		private int line;
		private int column;
		private int id;
		private int callStackStep;
		private String relevantPart;
		private String source;
		private Severity severity;
		private Integer cweId;

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

		public FindingNodeBuilder setId(int id) {
			this.id = id;
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

		public FindingNodeBuilder setSeverity(Severity severity) {
			this.severity = severity;
			return this;
		}

		public FindingNodeBuilder setCweId(Integer cweId) {
			this.cweId=cweId;
			return this;
		}
		
		public FindingNode build() {
			FindingNode node = new FindingNode(description, location, line, column, relevantPart, source, severity);
			node.callStackStep = callStackStep;
			node.id = id;
			node.cweId=cweId;

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
		return severity;
	}

	public int getId() {
		return id;
	}
	
	public Integer getCweId() {
		return cweId;
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
		 synchronized(monitor) {
			 if (metaDataCache==null) {
				 metaDataCache= new HashMap<>();
			 }
			 metaDataCache.put(key, value);
		 }
	}
	
	public Object getCachedMetaData(String key) {
		 synchronized(monitor) {
			 if (metaDataCache==null) {
				 metaDataCache= new HashMap<>();
			 }
			 return metaDataCache.get(key);
		 }
	}
	
	private Object monitor= new Object();

	@Override
	public String toString() {
		return "FindingNode [parent=" + parent + ", children=" + children + ", description=" + description
				+ ", location=" + location + ", line=" + line + ", column=" + column + ", relevantPart=" + relevantPart
				+ ", source=" + source + ", severity=" + severity + ", callStackStep=" + callStackStep + ", id=" + id
				+ "]";
	}

	@Override
	public int compareTo(FindingNode o) {
		if (o == null) {
			return 1;
		}
		return id - o.id;
	}

}
