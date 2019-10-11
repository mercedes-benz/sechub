package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoCodeCallStackElement {

	private String location;
	private Integer line;
	private Integer column;
	private String source;

	private SerecoCodeCallStackElement calls;

	public SerecoCodeCallStackElement getCalls() {
		return calls;
	}

	public void setCalls(SerecoCodeCallStackElement calls) {
		this.calls = calls;
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
		SerecoCodeCallStackElement other = (SerecoCodeCallStackElement) obj;
		return Objects.equals(calls, other.calls) && Objects.equals(column, other.column) && Objects.equals(line, other.line)
				&& Objects.equals(location, other.location) && Objects.equals(source, other.source);
	}

	@Override
	public String toString() {
		return "CodeInfo [location=" + location + ", line=" + line + ", column=" + column + ", source=" + source + "]";
	}

}
