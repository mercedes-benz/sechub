// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * "A run object describes a single run of an analysis tool and contains the
 * output of that run." see
 * 
 * <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317485">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonPropertyOrder({ "tool", "results" })
public class Run {
    private Tool tool;

    private List<Result> results;

    public Run() {
        results = new LinkedList<Result>();
    }

    public Run(Tool tool, List<Result> results) {
        this.tool = tool;
        this.results = results;
    }

    public void addResult(Result result) {
        results.add(result);
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "Run [tool=" + tool + ", results=" + results + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, tool);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Run other = (Run) obj;
        return Objects.equals(results, other.results) && Objects.equals(tool, other.tool);
    }
}
