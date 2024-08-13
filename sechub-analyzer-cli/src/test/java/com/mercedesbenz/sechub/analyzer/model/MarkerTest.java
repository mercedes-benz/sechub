// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.analyzer.model;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class MarkerTest {
    @Test
    public void test_deepClone() {
        /* prepare */
        Marker marker = new Marker(MarkerType.START, 3, 3);

        /* execute */
        Marker markerCopy = marker.deepClone();

        /* test */
        assertThat(marker, is(markerCopy));
        assertThat("Memory address is identical.", marker != markerCopy);
    }

    @Test
    public void test_equal__equal_markers() {
        /* prepare */
        Marker marker = new Marker(MarkerType.START, 3, 3);
        Marker marker2 = new Marker(MarkerType.START, 3, 3);

        /* test */
        assertThat(marker, is(marker2));
        assertThat("Memory address is identical.", marker != marker2);
    }

    @Test
    public void test_equal__unequal_markers() {
        /* prepare */
        Marker marker = new Marker(MarkerType.START, 3, 3);
        Marker marker2 = new Marker(MarkerType.END, 4, 3);

        /* test */
        assertThat(marker, is(not(marker2)));
        assertThat("Memory address is identical.", marker != marker2);
    }
}
