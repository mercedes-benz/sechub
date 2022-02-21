package com.mercedesbenz.sechub.domain.administration.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupConfig;
import com.mercedesbenz.sechub.domain.administration.autocleanup.AutoCleanupDaysCalculator;
import com.mercedesbenz.sechub.domain.administration.autocleanup.CountableInDaysTimeunit;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;

class AdministrationConfigServiceTest {

    private AdministrationConfigService toTest;
    private AdministrationConfigRepository repository;
    private AdministrationConfigTransactionService transactionService;
    private AutoCleanupDaysCalculator calculator;
    private DomainMessageService domainMessageService;

    @BeforeEach
    void beforeEach() {
        toTest = new AdministrationConfigService();
        repository = mock(AdministrationConfigRepository.class);
        transactionService = mock(AdministrationConfigTransactionService.class);
        calculator = new AutoCleanupDaysCalculator();
        domainMessageService = mock(DomainMessageService.class);

        toTest.repository = repository;
        toTest.transactionService = transactionService;
        toTest.calculator = calculator;
        toTest.domainMessageService = domainMessageService;
    }

    @Test
    void an_existing_config_is_updated_with_given_auto_cleanup_config() {
        AdministrationConfig existingAdministrationConfig = new AdministrationConfig();
        when(repository.findById(Integer.valueOf(0))).thenReturn(Optional.of(existingAdministrationConfig));
        when(transactionService.saveConfigInOwnTransaction(existingAdministrationConfig)).thenReturn(existingAdministrationConfig);

        AutoCleanupConfig autoCleanupConfiguration = new AutoCleanupConfig();

        /* execute */
        toTest.updateAutoCleanup(autoCleanupConfiguration);

        /* test */
        verify(repository).findById(0);
        ArgumentCaptor<AdministrationConfig> captor = ArgumentCaptor.forClass(AdministrationConfig.class);
        verify(transactionService).saveConfigInOwnTransaction(captor.capture());

        AdministrationConfig storedConfig = captor.getValue();
        assertEquals(storedConfig.autoCleanupConfiguration, autoCleanupConfiguration.toJSON());
    }

    @Test
    void an_existing_config_is_updated_with_null_throws_illegal_argument() {
        AdministrationConfig existingAdministrationConfig = new AdministrationConfig();
        when(repository.findById(Integer.valueOf(0))).thenReturn(Optional.of(existingAdministrationConfig));
        when(transactionService.saveConfigInOwnTransaction(existingAdministrationConfig)).thenReturn(existingAdministrationConfig);

        AutoCleanupConfig autoCleanupConfiguration = null;

        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> toTest.updateAutoCleanup(autoCleanupConfiguration));
    }

    @Test
    void a_not_existing_config_is_created_blank_and_then_updated_with_given_auto_cleanup_config() {
        AdministrationConfig createdAdministrationConfig = new AdministrationConfig();
        AutoCleanupConfig newAutoCleanConfiguration = new AutoCleanupConfig();
        newAutoCleanConfiguration.getCleanupTime().setAmount(1);
        newAutoCleanConfiguration.getCleanupTime().setUnit(CountableInDaysTimeunit.MONTH);
        when(repository.findById(0)).thenReturn(Optional.empty());
        when(transactionService.saveConfigInOwnTransaction(any())).thenReturn(createdAdministrationConfig);

        /* execute */
        toTest.updateAutoCleanup(newAutoCleanConfiguration);

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

}
