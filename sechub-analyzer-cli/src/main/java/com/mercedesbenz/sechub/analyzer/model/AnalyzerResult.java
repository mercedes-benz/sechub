// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * A container class for the analysis result.
 */
public class AnalyzerResult implements DeepClonable<AnalyzerResult> {

    private Map<String, List<MarkerPair>> noSecHubMarkers;

    public AnalyzerResult(Map<String, List<MarkerPair>> noSecHubMarkers) {
        this.noSecHubMarkers = noSecHubMarkers;
    }

    /**
     * Converts this result to JSON representation
     *
     * @param prettyPrint
     * @return JSON representation of this result
     * @throws JSONObjectException
     * @throws IOException
     */
    public String ToJSON(boolean prettyPrint) throws JSONObjectException, IOException {
        if (prettyPrint) {
            return JSON.std.with(Feature.PRETTY_PRINT_OUTPUT).asString(this);
        } else {
            return JSON.std.asString(this);
        }
    }

    /**
     * Get all "no sechub" markers
     *
     * @return
     */
    public Map<String, List<MarkerPair>> getNoSecHubMarkers() {
        return deepCopyNoSecHubMarkers();
    }

    public boolean hasResults() {
        return !noSecHubMarkers.isEmpty();
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
                MarkerPair markerPairCopy = markerPair.deepClone();

                markerPairsCopy.add(markerPairCopy);
            }

            noSecHubMarkersCopy.put(noSecHubMarkerEntry.getKey(), markerPairsCopy);
        }

        return noSecHubMarkersCopy;
    }
}
