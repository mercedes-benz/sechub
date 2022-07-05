// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;

class SchedulerConfigServiceTest {

    private SchedulerConfigService serviceToTest;
    private SchedulerConfigRepository repository;
    private DomainMessageService domainMessageService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new SchedulerConfigService();

        repository = mock(SchedulerConfigRepository.class);
        domainMessageService = mock(DomainMessageService.class);

        serviceToTest.repository = repository;
        serviceToTest.domainMessageService = domainMessageService;
    }

    @Test
    void get_auto_cleanup_in_days_fetches_information_from_database_initial_0() {
        emulateExistingInitialSchedulerConfig();

        /* execute */
        long result = serviceToTest.getAutoCleanupInDays();

        /* test */
        assertEquals(0, result);
    }

    @Test
    void get_auto_cleanup_in_days_fetches_information_from_database() {
        emulateExistingInitialSchedulerConfig().autoCleanupInDays = 42L;

        /* execute */
        long result = serviceToTest.getAutoCleanupInDays();

        /* test */
        assertEquals(42, result);
    }

    @Test
    void auto_cleanup_in_days_changed_stores_information_in_database() {
        emulateExistingInitialSchedulerConfig();

        /* execute */
        serviceToTest.updateAutoCleanupInDays(4711);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<SchedulerConfig> captor = ArgumentCaptor.forClass(SchedulerConfig.class);
        verify(repository).save(captor.capture());

        SchedulerConfig storedConfig = captor.getValue();
        assertEquals(4711, storedConfig.autoCleanupInDays);
    }

    private SchedulerConfig emulateExistingInitialSchedulerConfig() {
        /* initial configuration, no special setup: */
        SchedulerConfig existingSchedulerConfig = new SchedulerConfig();

        when(repository.findById(Integer.valueOf(0))).thenReturn(Optional.of(existingSchedulerConfig));
        when(repository.save(existingSchedulerConfig)).thenReturn(existingSchedulerConfig);

        return existingSchedulerConfig;
    }
}
