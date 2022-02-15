// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Region object, see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317685">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonPropertyOrder({ "startLine", "startColumn" })
public class Region extends SarifObject{
    private int startLine;
    private int startColumn;
    private ArtifactContent snippet;

    public Region() {
    }

    /**
     * Only values >= 0 make sense here, since it describes the line and column
     * numbers of a file where the finding was found at.
     * 
     * @param startLine   is a positive integer, see
     *                    https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317690
     * @param startColumn is a positive integer, see
     *                    https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317691
     * 
     * @throws IllegalArgumentException, if startLine < 0 or startColumn < 0.
     */
    public Region(int startLine, int startColumn) {
        if (startLine < 0 || startColumn < 0) {
            throw new IllegalArgumentException("Only values >= 0 are allowed. The start line/column describe a line/column number inside a file.");
        }
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    public int getStartLine() {
        return startLine;
    }

    /**
     * Only values >= 0 make sense here, since it describes the line of the file
     * where the finding was found at.
     * 
     * @param startLine
     * 
     * @throws IllegalArgumentException, if or startColumn < 0.
     */
    public void setStartLine(int startLine) {
        if (startLine < 0) {
            throw new IllegalArgumentException("Only values >= 0 are allowed. The start line describes a line number inside a file.");
        }
        this.startLine = startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Only values >= 0 make sense here, since it describes the column of the file
     * where the finding was found at.
     * 
     * @param startColumn
     * 
     * @throws IllegalArgumentException, if startLine < 0.
     */
    public void setStartColumn(int startColumn) {
        if (startColumn < 0) {
            throw new IllegalArgumentException("Only values >= 0 are allowed. The start column describes a column number inside a file.");
        }
        this.startColumn = startColumn;
    }
    
    
    /**
     * Set snippet property which is an artifactContent object - see https://docs.oasis-open.org/sarif/sarif/v2.1.0/csprd01/sarif-v2.1.0-csprd01.html#_Toc10541136
     * @param snippet
     */
    public void setSnippet(ArtifactContent snippet) {
        this.snippet = snippet;
    }
    
    public ArtifactContent getSnippet() {
        return snippet;
    }

    @Override
    public String toString() {
        return "Region [sartLine=" + startLine + ", startColumn=" + startColumn + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(startColumn, startLine);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Region other = (Region) obj;
        return startColumn == other.startColumn && startLine == other.startLine;
    }

}
