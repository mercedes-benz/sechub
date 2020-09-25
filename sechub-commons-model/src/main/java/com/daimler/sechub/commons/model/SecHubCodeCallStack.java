// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SecHubCodeCallStack {

	String location;

	Integer line;

	Integer column;

	String source;

	String relevantPart;

	SecHubCodeCallStack calls;

	public String getRelevantPart() {
		return relevantPart;
	}

	public void setRelevantPart(String relevantPart) {
		this.relevantPart = relevantPart;
	}


	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getColumn() {
		return column;
	}

	public void setColumn(Integer column) {
		this.column = column;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public SecHubCodeCallStack getCalls() {
		return calls;
	}

	public void setCalls(SecHubCodeCallStack calls) {
		this.calls = calls;
	}

	@Override
	public String toString() {
		return "SecHubCode [location=" + location + ", line=" + line + ", column=" + column + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(calls, column, line, location, source);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SecHubCodeCallStack other = (SecHubCodeCallStack) obj;
		return Objects.equals(calls, other.calls) && Objects.equals(column, other.column) && Objects.equals(line, other.line)
				&& Objects.equals(location, other.location) && Objects.equals(source, other.source);
	}

}
