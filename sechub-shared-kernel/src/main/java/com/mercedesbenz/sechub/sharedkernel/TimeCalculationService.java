package com.mercedesbenz.sechub.sharedkernel;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeCalculationService {

    @Autowired
    SystemTimeProvider systemTime;

    public LocalDateTime calculateNowMinusDays(Long days) {
        LocalDateTime now = systemTime.getNow();
        if (days == null) {
            return now;
        }
        return now.minusDays(days);
    }
}
