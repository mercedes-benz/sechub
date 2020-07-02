package com.daimler.analyzer.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * A container class for the analysis result.
 */
public class AnalyzerResult implements Copyable<AnalyzerResult> {

    private Map<String, List<MarkerPair>> noSecHubMarkers;

    public AnalyzerResult(Map<String, List<MarkerPair>> noSecHubMarkers) {
        this.noSecHubMarkers = noSecHubMarkers;
    }
    
    /**
     * Get all no sechub markers
     * 
     * @return
     */
    public Map<String, List<MarkerPair>> getNoSecHubMarkers() {
        return deepCopyNoSecHubMarkers();
    }
    
    public boolean hasResults() {
        return noSecHubMarkers.isEmpty();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(noSecHubMarkers);
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
        return Objects.equals(noSecHubMarkers, other.noSecHubMarkers);
    }

    @Override
    public String toString() {
        return "AnalyzerResult [noSecHubMarkers=" + noSecHubMarkers + "]";
    }

    @Override
    public AnalyzerResult deepClone() {       
        Map<String, List<MarkerPair>> noSecHubMarkersCopy = deepCopyNoSecHubMarkers();
        
        AnalyzerResult analyzerResultCopy = new AnalyzerResult(noSecHubMarkersCopy);
        
        return analyzerResultCopy;
    }
    
    private Map<String, List<MarkerPair>> deepCopyNoSecHubMarkers() {
        int initialeCapacity = noSecHubMarkers.size();
        Map<String, List<MarkerPair>> noSecHubMarkersCopy = new HashMap<String, List<MarkerPair>>(initialeCapacity);
        
        for (Entry<String, List<MarkerPair>> noSecHubMarkerEntry : noSecHubMarkers.entrySet()) {
          List<MarkerPair> markerPairsCopy = new LinkedList<>();
          
          List<MarkerPair> markerPairs = noSecHubMarkerEntry.getValue();
          
          for (MarkerPair markerPair : markerPairs) {
              System.out.println(markerPair);
              
              MarkerPair markerPairCopy = markerPair.deepClone();
              
              markerPairsCopy.add(markerPairCopy);
          }
          
          noSecHubMarkersCopy.put(noSecHubMarkerEntry.getKey(), markerPairsCopy);
        }
        
        return noSecHubMarkersCopy;
    }
}
