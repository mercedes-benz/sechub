// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

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
@JsonPropertyOrder({ "tool", "taxonomies", "versionControlProvenance", "results", "properties" })
public class Run extends SarifObject {
	private Tool tool;

	private List<Taxonomy> taxonomies;

	private List<VersionControlDetails> versionControlProvenance;

	private List<Result> results;

	public Run() {
		results = new LinkedList<>();
		taxonomies = new LinkedList<>();
		versionControlProvenance = new LinkedList<>();
	}

	public Run(Tool tool, List<Result> results, List<Taxonomy> taxonomies,
			List<VersionControlDetails> versionControlProvenance) {
		this.tool = tool;
		this.taxonomies = taxonomies;
		this.versionControlProvenance = versionControlProvenance;
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

	public List<Taxonomy> getTaxonomies() {
		return taxonomies;
	}

	public void setTaxonomies(List<Taxonomy> taxonomies) {
		this.taxonomies = taxonomies;
	}

	public List<VersionControlDetails> getVersionControlProvenance() {
		return versionControlProvenance;
	}

	public void setVersionControlProvenance(List<VersionControlDetails> versionControlProvenance) {
		this.versionControlProvenance = versionControlProvenance;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "Run [tool=" + tool + ", taxonomies=" + taxonomies + ", versionControlProvenance="
				+ versionControlProvenance + ", results=" + results + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(results, taxonomies, tool, versionControlProvenance);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Run)) {
			return false;
		}
		Run other = (Run) obj;
		return Objects.equals(results, other.results) && Objects.equals(taxonomies, other.taxonomies)
				&& Objects.equals(tool, other.tool)
				&& Objects.equals(versionControlProvenance, other.versionControlProvenance)
				&& Objects.equals(getProperties(), other.getProperties());
	}

}
