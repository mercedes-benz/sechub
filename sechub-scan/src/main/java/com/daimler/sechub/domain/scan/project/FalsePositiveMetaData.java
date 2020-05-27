package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

import com.daimler.sechub.domain.scan.Severity;
import com.daimler.sechub.sharedkernel.type.ScanType;

public class FalsePositiveMetaData {

    private ScanType scanType;
    private String name;
    private Severity severity;
    private FalsePositiveCodeMetaData code;
    
    public FalsePositiveCodeMetaData getCode() {
        return code;
    }
    
    public void setCode(FalsePositiveCodeMetaData code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    @Override
    public String toString() {
        return "FalsePositiveMetaData [scanType=" + scanType + ", name=" + name + ", severity=" + severity + ", code=" + code + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, scanType, severity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FalsePositiveMetaData other = (FalsePositiveMetaData) obj;
        return Objects.equals(code, other.code) && Objects.equals(name, other.name) && scanType == other.scanType && severity == other.severity;
    }

}
