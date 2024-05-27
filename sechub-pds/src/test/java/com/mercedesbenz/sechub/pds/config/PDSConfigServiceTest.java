// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConfig;
import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupConfig.CleanupTime;
import com.mercedesbenz.sechub.pds.autocleanup.PDSAutoCleanupDaysCalculator;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.CountableInDaysTimeUnit;

class PDSConfigServiceTest {

    private PDSConfigService serviceToTest;
    private PDSConfigRepository repository;
    private PDSConfigTransactionService transactionService;
    private PDSAutoCleanupDaysCalculator calculator;
    private PDSLogSanitizer PDSLogSanitizer;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new PDSConfigService();

        repository = mock(PDSConfigRepository.class);
        transactionService = mock(PDSConfigTransactionService.class);
        calculator = mock(PDSAutoCleanupDaysCalculator.class);
        PDSLogSanitizer = mock(PDSLogSanitizer.class);

        serviceToTest.repository = repository;
        serviceToTest.transactionService = transactionService;
        serviceToTest.calculator = calculator;
        serviceToTest.PDSLogSanitizer = PDSLogSanitizer;
    }

    @Test
    void get_auto_cleanup_in_days_fetches_information_from_database_initial_0() {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        /* execute */
        long result = serviceToTest.getAutoCleanupInDays();

        /* test */
        assertEquals(0, result);
    }

    @Test
    void get_auto_cleanup_in_days_fetches_information_from_database() {
        /* prepare */
        emulateExistingInitialAdministrationConfig().autoCleanupInDays = 42L;

        /* execute */
        long result = serviceToTest.getAutoCleanupInDays();

        /* test */
        assertEquals(42, result);
    }

    @Test
    void auto_cleanup_in_days_changed_stores_information_in_database() {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        /* execute */
        serviceToTest.updateAutoCleanupInDays(4711);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<PDSConfig> captor = ArgumentCaptor.forClass(PDSConfig.class);
        verify(transactionService).saveConfigInOwnTransaction(captor.capture());

        PDSConfig storedConfig = captor.getValue();
        assertEquals(4711, storedConfig.autoCleanupInDays);
    }

    @Test
    void an_existing_config_is_updated_with_given_auto_cleanup_config() {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        PDSAutoCleanupConfig autoCleanupConfiguration = new PDSAutoCleanupConfig();

        /* execute */
        serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<PDSConfig> captor = ArgumentCaptor.forClass(PDSConfig.class);
        verify(transactionService).saveConfigInOwnTransaction(captor.capture());

        PDSConfig storedConfig = captor.getValue();
        assertEquals(storedConfig.autoCleanupConfiguration, autoCleanupConfiguration.toJSON());
    }

    @ParameterizedTest
    @CsvSource({ "-1", "-100", "-4711" })
    void when_calculator_calculates_an_negative_days_count_a_non_acceptable_exception_is_thrown(long calculatedDays) {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        PDSAutoCleanupConfig autoCleanupConfiguration = new PDSAutoCleanupConfig();
        when(calculator.calculateCleanupTimeInDays(autoCleanupConfiguration)).thenReturn(calculatedDays);

        /* execute + test */
        assertThrows(PDSNotAcceptableException.class, () -> serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration));
    }

    @Test
    void an_existing_config_is_updated_with_null_throws_illegal_argument() {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        PDSAutoCleanupConfig autoCleanupConfiguration = null;

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration));
    }

    @Test
    void a_not_existing_config_is_created_with_defaults_and_then_updated_with_given_auto_cleanup_config() {
        /* prepare */
        emulateMissingAdministrationConfigCreated();
        PDSAutoCleanupConfig newAutoCleanConfiguration = new PDSAutoCleanupConfig();
        newAutoCleanConfiguration.getCleanupTime().setAmount(1);
        newAutoCleanConfiguration.getCleanupTime().setUnit(CountableInDaysTimeUnit.MONTH);

        /* execute */
        serviceToTest.updateAutoCleanupConfiguration(newAutoCleanConfiguration);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<PDSConfig> captor = ArgumentCaptor.forClass(PDSConfig.class);
        verify(transactionService, times(2)).saveConfigInOwnTransaction(captor.capture());

        List<PDSConfig> storedConfigs = captor.getAllValues();
        assertEquals(2, storedConfigs.size());
        PDSConfig firstStored = storedConfigs.get(0);
        PDSConfig secondStored = storedConfigs.get(1);

        assertNotNull(firstStored.autoCleanupConfiguration);
        assertNotNull(secondStored.autoCleanupConfiguration);
        assertEquals(newAutoCleanConfiguration.toJSON(), secondStored.autoCleanupConfiguration);

        PDSAutoCleanupConfig expectedBefore = new PDSAutoCleanupConfig();
        CleanupTime cleanupTimeBefore = expectedBefore.getCleanupTime();
        cleanupTimeBefore.setAmount(2);
        cleanupTimeBefore.setUnit(CountableInDaysTimeUnit.DAY);

        assertEquals(expectedBefore.toJSON(), firstStored.autoCleanupConfiguration);
    }

    @Test
    void for_a_not_existing_config_getAutoCleanupInDays_config_with_2days_is_used_as_default_and_stored_() {
        /* prepare */
        emulateMissingAdministrationConfigCreated();

        PDSConfig configStoredByTransactionService = new PDSConfig();
        configStoredByTransactionService.autoCleanupInDays = (long) 12345;
        ArgumentCaptor<PDSConfig> transactionParameterCaptor = ArgumentCaptor.forClass(PDSConfig.class);
        when(transactionService.saveConfigInOwnTransaction(transactionParameterCaptor.capture())).thenReturn(configStoredByTransactionService);

        long calculatedDays = 987L;
        when(calculator.calculateCleanupTimeInDays(any())).thenReturn(calculatedDays);

        /* execute */
        long days = serviceToTest.getAutoCleanupInDays();

        /* test */
        PDSConfig configGivenToTransactionService = transactionParameterCaptor.getValue();

        PDSAutoCleanupConfig autoCleanConfigGivenToTransactionService = PDSAutoCleanupConfig
                .fromString(configGivenToTransactionService.autoCleanupConfiguration);
        // stored default fallback clean up time was 2 days:
        CleanupTime timeToStore = autoCleanConfigGivenToTransactionService.getCleanupTime();
        assertEquals(2, timeToStore.getAmount());
        assertEquals(CountableInDaysTimeUnit.DAY, timeToStore.getUnit());

        // while storing with transaction service, the calculated value is used
        assertEquals(calculatedDays, configGivenToTransactionService.getAutoCleanupInDays());

        assertEquals(configStoredByTransactionService.autoCleanupInDays, days); // the stored entity value is returned...
    }

    private void emulateMissingAdministrationConfigCreated() {
        PDSConfig createdAdministrationConfig = new PDSConfig();
        when(transactionService.saveConfigInOwnTransaction(any())).thenReturn(createdAdministrationConfig);
    }

    private PDSConfig emulateExistingInitialAdministrationConfig() {
        /* initial configuration, no special setup: */
        PDSConfig existingAdministrationConfig = new PDSConfig();

        when(repository.findById(Integer.valueOf(0))).thenReturn(Optional.of(existingAdministrationConfig));
        when(transactionService.saveConfigInOwnTransaction(existingAdministrationConfig)).thenReturn(existingAdministrationConfig);

        return existingAdministrationConfig;
    }

}
