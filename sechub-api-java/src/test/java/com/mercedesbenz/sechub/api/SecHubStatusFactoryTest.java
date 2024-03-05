package com.mercedesbenz.sechub.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.api.SecHubStatus.JobsOverviewData;
import com.mercedesbenz.sechub.api.SecHubStatus.SchedulerData;

class SecHubStatusFactoryTest {

    private SecHubStatusFactory factoryToTest;

    @BeforeEach
    void beforeEach() {
        factoryToTest = new SecHubStatusFactory();
    }

    @Test
    void createFromMap_filled_map() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("status.scheduler.enabled", "true");
        map.put("status.scheduler.jobs.all", "13");
        map.put("status.scheduler.jobs.started", "1");
        map.put("status.scheduler.jobs.ended", "2");
        map.put("status.scheduler.jobs.ready_to_start", "3");
        map.put("status.scheduler.jobs.cancel_requested", "4");
        map.put("status.scheduler.jobs.canceled", "5");
        map.put("status.scheduler.jobs.initializing", "6");

        /* execute */
        SecHubStatus result = factoryToTest.createFromMap(map);

        /* test */
        SchedulerData scheduler = result.getScheduler();
        assertTrue(scheduler.isEnabled());

        JobsOverviewData jobs = scheduler.getJobs();

        assertEquals(13, jobs.getAll());
        assertEquals(1, jobs.getStarted());
        assertEquals(2, jobs.getEnded());
        assertEquals(3, jobs.getReadyToStart());
        assertEquals(4, jobs.getCancelRequested());
        assertEquals(5, jobs.getCanceled());
        assertEquals(6, jobs.getInitializating());

    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void createFromMap_empty_map(Map<String, String> map) {

        /* execute */
        SecHubStatus result = factoryToTest.createFromMap(map);

        /* test */
        SchedulerData scheduler = result.getScheduler();
        assertFalse(scheduler.isEnabled());

        JobsOverviewData jobs = scheduler.getJobs();

        assertEquals(0, jobs.getAll());
        assertEquals(0, jobs.getInitializating());
        assertEquals(0, jobs.getCanceled());
        assertEquals(0, jobs.getCancelRequested());
        assertEquals(0, jobs.getEnded());
        assertEquals(0, jobs.getReadyToStart());
        assertEquals(0, jobs.getStarted());

    }

}
