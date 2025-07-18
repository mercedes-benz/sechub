// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.api.internal.gen.model.Severity;

/**
 * The FindingNode represents data structure required in SecHubReportView
 */
public class FindingNode implements Comparable<FindingNode> {

    private Object monitor = new Object();
    private FindingNode parent = null;

    private SecHubFinding secHubFinding;

    private List<FindingNode> children = new LinkedList<FindingNode>();

    private Integer cweId;
    private String name;

    private String description;
    String location;
    private Integer line;
    private Integer column;
    private String relevantPart;
    private String source;
    private Severity severity;
    public Integer callStackStep;
    public Integer id;
    private String fileName;
    public String filePath;
    private Map<String, Object> metaDataCache;

    private ScanType scanType;
    private String solution;

    private FindingNode() {
    }

    public static class FindingNodeBuilder {
        private String name;
        private String description;
        private String location;
        private Integer line;
        private Integer column;
        private Integer id;
        private Integer callStackStep;
        private String relevantPart;
        private String source;
        private Severity severity;
        private Integer cweId;

        private ScanType scanType;
        private String solution;
        private SecHubFinding secHubFinding;

        private FindingNodeBuilder() {

        }

        public FindingNodeBuilder setScanType(ScanType scanType) {
            this.scanType = scanType;
            return this;
        }

        public FindingNodeBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public FindingNodeBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public FindingNodeBuilder setSolution(String solution) {
            this.solution = solution;
            return this;
        }

        public FindingNodeBuilder setLocation(String location) {
            this.location = location;
            return this;
        }

        public FindingNodeBuilder setLine(Integer line) {
            this.line = line;
            return this;
        }

        public FindingNodeBuilder setColumn(Integer column) {
            this.column = column;
            return this;
        }

        public FindingNodeBuilder setCallStackStep(Integer callStackStep) {
            this.callStackStep = callStackStep;
            return this;
        }

        public FindingNodeBuilder setId(Integer id) {
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
            this.cweId = cweId;
            return this;
        }

        public FindingNodeBuilder setSecHubFinding(SecHubFinding secHubFinding) {
            this.secHubFinding = secHubFinding;
            return this;
        }

        public FindingNode build() {
            FindingNode node = new FindingNode();
            node.description = description;
            node.location = location;
            node.name = name;
            node.line = line;
            node.column = column;
            node.relevantPart = relevantPart;
            node.source = source;
            node.severity = severity;
            node.callStackStep = callStackStep;
            node.id = id;
            node.cweId = cweId;
            node.scanType = scanType;
            node.solution = solution;
            node.secHubFinding = secHubFinding;

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

    public String getSolution() {
        return solution;
    }

    public SecHubFinding getSecHubFinding() {
        return secHubFinding;
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

    public String getName() {
        return name;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public boolean canBeShownInCallHierarchy() {
        ScanType scanType = getScanType();
        if (ScanType.WEB_SCAN.equals(scanType)) {
            return false;
        }
        if (ScanType.INFRA_SCAN.equals(scanType)) {
            return false;
        }
        return true;
    }

    public boolean canBeShownInWebRequest() {
        ScanType scanType = getScanType();
        if (ScanType.WEB_SCAN.equals(scanType)) {
            return true;
        }
        return false;
    }

    public boolean canBeShownInWebResponse() {
        ScanType scanType = getScanType();
        if (ScanType.WEB_SCAN.equals(scanType)) {
            return true;
        }
        return false;
    }

    public boolean canBeShownInAttack() {
        ScanType scanType = getScanType();
        if (ScanType.WEB_SCAN.equals(scanType)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FindingNode [parent=" + parent + ", children=" + children + ", description=" + description + ", location=" + location + ", line=" + line
                + ", column=" + column + ", relevantPart=" + relevantPart + ", source=" + source + ", severity=" + severity + ", callStackStep=" + callStackStep
                + ", id=" + id + "]";
    }

    @Override
    public int compareTo(FindingNode o) {
        if (o == null) {
            return 1;
        }
        return id - o.id;
    }

}
