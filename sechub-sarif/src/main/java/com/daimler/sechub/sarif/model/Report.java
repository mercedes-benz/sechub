// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Sarif format documentation available at
 * <ul>
 * <li>https://sarifweb.azurewebsites.net/</li>
 * <li>https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html</li>
 * 
 * </ul>
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonPropertyOrder({ "version", "$schema", "runs" })
public class Report {
    private String version;
    private String $schema;

    private List<Run> runs;

    public Report() {
        this.runs = new LinkedList<Run>();
    }

    public Report(SarifVersion sarifVersion) {
        if (sarifVersion != null) {
            this.version = sarifVersion.getVersion();
            this.$schema = sarifVersion.getSchema();
        }
        this.runs = new LinkedList<Run>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String get$schema() {
        return $schema;
    }

    public void set$schema(String $schema) {
        this.$schema = $schema;
    }

    public List<Run> getRuns() {
        return runs;
    }

    public void setRuns(List<Run> runs) {
        this.runs = runs;
    }

    @Override
    public String toString() {
        return "Report [version=" + version + ", schema=" + $schema + ", runs=" + runs + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash($schema, runs, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Report other = (Report) obj;
        return Objects.equals($schema, other.$schema) && Objects.equals(runs, other.runs) && Objects.equals(version, other.version);
    }
}
