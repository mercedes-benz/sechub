package com.daimler.analyzer.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnalyzerResult {

    private Map<String, List<MarkerPair>> findings;

    public AnalyzerResult(Map<String, List<MarkerPair>> findings) {
        this.findings = findings;
    }
    
    public Map<String, List<MarkerPair>> getFindings() {
        return findings;
    }
    
    public boolean hasResults() {
        return !getFindings().isEmpty();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(findings);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AnalyzerResult other = (AnalyzerResult) obj;
        return Objects.equals(findings, other.findings);
    }

    @Override
    public String toString() {
        return "AnalyzerResult [result=" + findings + "]";
    }
}
