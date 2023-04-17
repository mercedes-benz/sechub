// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoCodeCallStackElement {

    private String location;

    private Integer line = 0;

    private Integer column = 0;

    private String source;

    private String relevantPart;

    private SerecoCodeCallStackElement calls;

    public void setRelevantPart(String relevantPart) {
        this.relevantPart = relevantPart;
    }

    public String getRelevantPart() {
        return relevantPart;
    }

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

    /**
     * Set value of line to the value of parameter line, if the parameter line is
     * NOT null
     *
     * @param line
     */
    public void setLine(Integer line) {
        if (line != null) {
            this.line = line;
        }
    }

    public Integer getColumn() {
        return column;
    }

    /**
     * Set value of column to the value of parameter column, if the parameter column
     * is NOT null
     *
     * @param column
     */
    public void setColumn(Integer column) {
        if (column != null) {
            this.column = column;
        }
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
