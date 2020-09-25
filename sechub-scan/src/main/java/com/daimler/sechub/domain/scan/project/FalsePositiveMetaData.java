// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.commons.model.Severity;

public class FalsePositiveMetaData {
    public static final String PROPERTY_SCANTYPE="scanType";
    public static final String PROPERTY_NAME="name";
    public static final String PROPERTY_CWE_ID="cweId";
    public static final String PROPERTY_CVE_ID="cveId";
    public static final String PROPERTY_OWASP="owasp";
    public static final String PROPERTY_SEVERITY="severity";
    public static final String PROPERTY_CODE="code";
    
    private ScanType scanType;
    private String name;
    private Severity severity;
    
    private FalsePositiveCodeMetaData code;

    private Integer cweId;
    private String cveId;
    private String owasp;

    public void setOwasp(String owasp) {
        this.owasp = owasp;
    }
    
    public String getOwasp() {
        return owasp;
    }
    
    public String getCveId() {
        return cveId;
    }
    
    public void setCveId(String cveId) {
        this.cveId = cveId;
    }
    
    /*
     * Returns common weakness enumeration ID, see https://cwe.mitre.org/
     */
    public Integer getCweId() {
        return cweId;
    }

    /*
     * set common weakness enumeration ID, see https://cwe.mitre.org/
     */
    public void setCweId(Integer cweId) {
        this.cweId = cweId;
    }
    
    
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
