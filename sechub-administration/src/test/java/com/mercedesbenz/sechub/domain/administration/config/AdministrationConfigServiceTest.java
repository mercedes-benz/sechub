package com.mercedesbenz.sechub.domain.administration.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupConfig;
import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupDaysCalculator;
import com.mercedesbenz.sechub.domain.administration.autocleanup.CountableInDaysTimeunit;
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
    private AutoCleanupDaysCalculator calculator;
    private DomainMessageService domainMessageService;
    private AuditLogService auditLogService;
    private LogSanitizer logSanitizer;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new AdministrationConfigService();

        repository = mock(AdministrationConfigRepository.class);
        transactionService = mock(AdministrationConfigTransactionService.class);
        calculator = mock(AutoCleanupDaysCalculator.class);
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
        emulateExistingInitialAdministrationConfig();

        /* execute */
        long result = serviceToTest.getAutoCleanupInDays();

        /* test */
        assertEquals(0, result);
    }

    @Test
    void get_auto_cleanup_in_days_fetches_information_from_database() {
        emulateExistingInitialAdministrationConfig().autoCleanupInDays = 42L;

        /* execute */
        long result = serviceToTest.getAutoCleanupInDays();

        /* test */
        assertEquals(42, result);
    }

    @Test
    void auto_cleanup_in_days_changed_stores_information_in_database() {
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
        emulateExistingInitialAdministrationConfig();
        long days = 42;
        AutoCleanupConfig autoCleanupConfiguration = new AutoCleanupConfig();
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
        emulateExistingInitialAdministrationConfig();

        AutoCleanupConfig autoCleanupConfiguration = new AutoCleanupConfig();

        /* execute */
        serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<AdministrationConfig> captor = ArgumentCaptor.forClass(AdministrationConfig.class);
        verify(transactionService).saveConfigInOwnTransaction(captor.capture());

        AdministrationConfig storedConfig = captor.getValue();
        assertEquals(storedConfig.autoCleanupConfiguration, autoCleanupConfiguration.toJSON());
    }

    @Test
    void an_existing_config_is_updated_with_null_throws_illegal_argument() {
        emulateExistingInitialAdministrationConfig();

        AutoCleanupConfig autoCleanupConfiguration = null;

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.updateAutoCleanupConfiguration(autoCleanupConfiguration));
    }

    @Test
    void a_not_existing_config_is_created_blank_and_then_updated_with_given_auto_cleanup_config() {
        emulateMissingAdministrationConfigCreated();
        AutoCleanupConfig newAutoCleanConfiguration = new AutoCleanupConfig();
        newAutoCleanConfiguration.getCleanupTime().setAmount(1);
        newAutoCleanConfiguration.getCleanupTime().setUnit(CountableInDaysTimeunit.MONTH);

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
        assertEquals(new AutoCleanupConfig().toJSON(), firstStored.autoCleanupConfiguration);
    }

    private void emulateMissingAdministrationConfigCreated() {
        AdministrationConfig createdAdministrationConfig = new AdministrationConfig();
        when(transactionService.saveConfigInOwnTransaction(any())).thenReturn(createdAdministrationConfig);
//        when(repository.findById(0)).thenReturn(Optional.empty());
    }

    private AdministrationConfig emulateExistingInitialAdministrationConfig() {
        /* initial configuration, no special setup: */
        AdministrationConfig existingAdministrationConfig = new AdministrationConfig();

        when(repository.findById(Integer.valueOf(0))).thenReturn(Optional.of(existingAdministrationConfig));
        when(transactionService.saveConfigInOwnTransaction(existingAdministrationConfig)).thenReturn(existingAdministrationConfig);

        return existingAdministrationConfig;
    }

}
