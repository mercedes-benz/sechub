// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.model;

public class Marker implements DeepClonable<Marker> {

    private long line;
    private long column;
    private MarkerType type;

    public Marker(MarkerType type, long line, long column) {
        this.type = type;
        this.line = line;
        this.column = column;
    }

    public long getLine() {
        return line;
    }

    public long getColumn() {
        return column;
    }

    public MarkerType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Marker [line=" + line + ", column=" + column + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (column ^ (column >>> 32));
        result = prime * result + (int) (line ^ (line >>> 32));
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Marker other = (Marker) obj;
        if (column != other.column)
            return false;
        if (line != other.line)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public Marker deepClone() {
        Marker marker = new Marker(type, line, column);

        return marker;
    }
}
