// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.model;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class MarkerPairTest {
    @Test
    public void test_deepClone() {
        /* prepare */
        Marker start = new Marker(MarkerType.START, 3, 3);
        Marker end = new Marker(MarkerType.START, 300, 900);

        MarkerPair markerPair = new MarkerPair();
        markerPair.setStart(start);
        markerPair.setEnd(end);

        /* execute */
        MarkerPair markerPairCopy = markerPair.deepClone();

        /* test */
        assertThat(markerPair, is(markerPairCopy));
        assertThat("Memory address is identical.", markerPair != markerPairCopy);
    }
}
