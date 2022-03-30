// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.config;

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

import com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupConfig;
import com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupDaysCalculator;
import com.mercedesbenz.sechub.sharedkernel.CountableInDaysTimeUnit;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.logging.LogSanitizer;
import com.mercedesbenz.sechub.sharedkernel.messaging.AdministrationConfigMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;

class AdministrationConfigServiceTest {

    private AdministrationConfigService serviceToTest;
    private AdministrationConfigRepository repository;
    private AdministrationConfigTransactionService transactionService;
    private AdministrationAutoCleanupDaysCalculator calculator;
    private DomainMessageService domainMessageService;
    private AuditLogService auditLogService;
    private LogSanitizer logSanitizer;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new AdministrationConfigService();

        repository = mock(AdministrationConfigRepository.class);
        transactionService = mock(AdministrationConfigTransactionService.class);
        calculator = mock(AdministrationAutoCleanupDaysCalculator.class);
        domainMessageService = mock(DomainMessageService.class);
        auditLogService = mock(AuditLogService.class);
        logSanitizer = mock(LogSanitizer.class);

        serviceToTest.repository = repository;
        serviceToTest.transactionService = transactionService;
        serviceToTest.calculator = calculator;
        serviceToTest.domainMessageService = domainMessageService;
        serviceToTest.auditLogService = auditLogService;
        serviceToTest.logSanitizer = logSanitizer;
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
        ArgumentCaptor<AdministrationConfig> captor = ArgumentCaptor.forClass(AdministrationConfig.class);
        verify(transactionService).saveConfigInOwnTransaction(captor.capture());

        AdministrationConfig storedConfig = captor.getValue();
        assertEquals(4711, storedConfig.autoCleanupInDays);
    }

    @Test
    void auto_cleanup_configuration_change_does_result_in_auto_cleanup_change_event_with_correct_data() {
        /* prepare */
        emulateExistingInitialAdministrationConfig();
        long days = 42;
        AdministrationAutoCleanupConfig autoCleanupConfiguration = new AdministrationAutoCleanupConfig();
        when(calculator.calculateCleanupTimeInDays(eq(autoCleanupConfiguration))).thenReturn(days);

        /* execute */
        serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration);

        /* test */
        ArgumentCaptor<DomainMessage> captor = ArgumentCaptor.forClass(DomainMessage.class);
        verify(domainMessageService).sendAsynchron(captor.capture());

        DomainMessage message = captor.getValue();
        AdministrationConfigMessage cleanupData = message.get(MessageDataKeys.AUTO_CLEANUP_CONFIG_CHANGE_DATA);
        assertNotNull(cleanupData);
        assertEquals(days, cleanupData.getAutoCleanupInDays());
    }

    @Test
    void an_existing_config_is_updated_with_given_auto_cleanup_config() {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        AdministrationAutoCleanupConfig autoCleanupConfiguration = new AdministrationAutoCleanupConfig();

        /* execute */
        serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<AdministrationConfig> captor = ArgumentCaptor.forClass(AdministrationConfig.class);
        verify(transactionService).saveConfigInOwnTransaction(captor.capture());

        AdministrationConfig storedConfig = captor.getValue();
        assertEquals(storedConfig.autoCleanupConfiguration, autoCleanupConfiguration.toJSON());
    }

    @ParameterizedTest
    @CsvSource({ "-1", "-100", "-4711" })
    void when_calculator_calculates_an_negative_days_count_a_non_acceptable_exception_is_thrown(long calculatedDays) {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        AdministrationAutoCleanupConfig autoCleanupConfiguration = new AdministrationAutoCleanupConfig();
        when(calculator.calculateCleanupTimeInDays(autoCleanupConfiguration)).thenReturn(calculatedDays);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration));
    }

    @ParameterizedTest
    @CsvSource({ "0", "1", "100", "4711" })
    void when_calculator_calculates_a_positive_day_count_or_zero_no__exception_is_thrown_and_an_event_sent(long calculatedDays) {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        AdministrationAutoCleanupConfig autoCleanupConfiguration = new AdministrationAutoCleanupConfig();
        when(calculator.calculateCleanupTimeInDays(autoCleanupConfiguration)).thenReturn(calculatedDays);

        /* execute */
        serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration);

        /* test */
        verify(domainMessageService).sendAsynchron(any());
    }

    @Test
    void an_existing_config_is_updated_with_null_throws_illegal_argument() {
        /* prepare */
        emulateExistingInitialAdministrationConfig();

        AdministrationAutoCleanupConfig autoCleanupConfiguration = null;

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration));
    }

    @Test
    void a_not_existing_config_is_created_blank_and_then_updated_with_given_auto_cleanup_config() {
        /* prepare */
        emulateMissingAdministrationConfigCreated();
        AdministrationAutoCleanupConfig newAutoCleanConfiguration = new AdministrationAutoCleanupConfig();
        newAutoCleanConfiguration.getCleanupTime().setAmount(1);
        newAutoCleanConfiguration.getCleanupTime().setUnit(CountableInDaysTimeUnit.MONTH);

        /* execute */
        serviceToTest.updateAutoCleanupConfiguration(newAutoCleanConfiguration);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<AdministrationConfig> captor = ArgumentCaptor.forClass(AdministrationConfig.class);
        verify(transactionService, times(2)).saveConfigInOwnTransaction(captor.capture());

        List<AdministrationConfig> storedConfigs = captor.getAllValues();
        assertEquals(2, storedConfigs.size());
        AdministrationConfig firstStored = storedConfigs.get(0);
        AdministrationConfig secondStored = storedConfigs.get(1);

        assertNotNull(firstStored.autoCleanupConfiguration);
        assertNotNull(secondStored.autoCleanupConfiguration);
        assertEquals(newAutoCleanConfiguration.toJSON(), secondStored.autoCleanupConfiguration);
        assertEquals(new AdministrationAutoCleanupConfig().toJSON(), firstStored.autoCleanupConfiguration);
    }

    private void emulateMissingAdministrationConfigCreated() {
        AdministrationConfig createdAdministrationConfig = new AdministrationConfig();
        when(transactionService.saveConfigInOwnTransaction(any())).thenReturn(createdAdministrationConfig);
    }

    private AdministrationConfig emulateExistingInitialAdministrationConfig() {
        /* initial configuration, no special setup: */
        AdministrationConfig existingAdministrationConfig = new AdministrationConfig();

        when(repository.findById(Integer.valueOf(0))).thenReturn(Optional.of(existingAdministrationConfig));
        when(transactionService.saveConfigInOwnTransaction(existingAdministrationConfig)).thenReturn(existingAdministrationConfig);

        return existingAdministrationConfig;
    }

}
