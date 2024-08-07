// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.model;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class AnalyzerResultTest {

    @Test
    public void deepClone_results_in_equal_but_not_same_analyzer_result() {
        /* prepare */
        Map<String, List<MarkerPair>> noSecHubMarkers = getNoSecHubMarkers();

        AnalyzerResult analyzerResult = new AnalyzerResult(noSecHubMarkers);

        /* execute */
        AnalyzerResult analyzerResultCopy = analyzerResult.deepClone();

        /* test */
        assertThat(analyzerResult, is(analyzerResultCopy));
        assertThat("Memory address is identical.", analyzerResult != analyzerResultCopy);
    }

    @Test
    public void getNoSecHubMarkers_returns_markers_given_at_construction_time_but_has_own_list() {
        /* prepare */
        Map<String, List<MarkerPair>> noSecHubMarkers = getNoSecHubMarkers();

        AnalyzerResult analyzerResult = new AnalyzerResult(noSecHubMarkers);

        /* execute */
        Map<String, List<MarkerPair>> noSecHubMarkersCopy = analyzerResult.getNoSecHubMarkers();

        /* test */
        assertThat(noSecHubMarkers, is(noSecHubMarkersCopy));
        assertThat("Memory address is identical.", noSecHubMarkers != noSecHubMarkersCopy);
    }

    /*
     * Helper method to create secHubMarkers
     */
    private Map<String, List<MarkerPair>> getNoSecHubMarkers() {
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.START, 300, 900);

        MarkerPair markerPair = new MarkerPair();
        markerPair.setStart(start);
        markerPair.setEnd(end);

        Marker start2 = new Marker(MarkerType.START, 4, 8);
        Marker end2 = new Marker(MarkerType.START, 7, 23);

        MarkerPair markerPair2 = new MarkerPair();
        markerPair2.setStart(start2);
        markerPair2.setEnd(end2);

        List<MarkerPair> markers = new LinkedList<>();
        markers.add(markerPair);
        markers.add(markerPair2);

        Map<String, List<MarkerPair>> noSecHubMarkers = new HashMap<>();
        noSecHubMarkers.put("/path/to/file", markers);

        return noSecHubMarkers;
    }
}
