package com.mercedesbenz.sechub.domain.statistic.job;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.TrafficLight;

@Service
public class JobRunStatisticService {

    public void storeJobDone(UUID jobUUID, LocalDateTime done, TrafficLight trafficLight) {

    }

    public void storeJobFailed(UUID jobUUID, LocalDateTime done, TrafficLight trafficLight) {

    }

}
