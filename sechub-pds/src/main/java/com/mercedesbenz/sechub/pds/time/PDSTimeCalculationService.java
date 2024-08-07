// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.time;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PDSTimeCalculationService {

    @Autowired
    PDSSystemTimeProvider systemTime;

    /**
     * Calculates current time stamp minus days. When days are negative the days are
     * added instead.
     *
     * @param days
     * @return time stamp, never <code>null</code>
     */
    public LocalDateTime calculateNowMinusDays(Long days) {
        LocalDateTime now = systemTime.getNow();
        if (days == null) {
            return now;
        }
        return now.minusDays(days);
    }
}
