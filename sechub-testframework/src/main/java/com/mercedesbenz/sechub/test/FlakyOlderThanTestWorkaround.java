package com.mercedesbenz.sechub.test;

import java.time.LocalDateTime;

/**
 * We had some tests which were flaky when it comes to "older than" calculation,
 * The suspicion lies on the h2 database and the precision of the timestamps
 * which is only integer.
 *
 * @author Albert Tregnaghi
 *
 */
public class FlakyOlderThanTestWorkaround {

    /**
     * Returns a local date time object which is one second "older" than the given
     * oldest.
     *
     * @param oldest
     * @return local date time object
     */
    public static LocalDateTime olderThanForDelete(LocalDateTime oldest) {
        return oldest.minusSeconds(1);
    }
}
