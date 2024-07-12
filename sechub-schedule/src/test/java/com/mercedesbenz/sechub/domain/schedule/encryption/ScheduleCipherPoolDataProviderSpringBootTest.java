package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = ScheduleCipherPoolDataProvider.class)
class ScheduleCipherPoolDataProviderSpringBootTest {

    @Autowired
    private ScheduleCipherPoolDataProvider providerToTest;

    @MockBean
    private ScheduleCipherPoolDataRepository repository;

    @BeforeEach
    void beforeEach() {
    }

    @Test
    void provider_creates_fallback_when_nothing_found() {

        /* prepare */
        when(repository.findAll()).thenReturn(Collections.emptyList());

        /* execute */
        List<ScheduleCipherPoolData> result = providerToTest.ensurePoolDataAvailable();

        /* test */
        assertThat(result).isNotEmpty().hasSize(1);
        ArgumentCaptor<ScheduleCipherPoolData> argumentCaptor = ArgumentCaptor.forClass(ScheduleCipherPoolData.class);
        verify(repository).save(argumentCaptor.capture());

        ScheduleCipherPoolData savedFallback = argumentCaptor.getValue();

        assertThat(savedFallback).isNotNull();

        assertThat(savedFallback.getAlgorithm()).isEqualTo(CipherAlgorithm.NONE);
        assertThat(savedFallback.getTestEncrypted()).isNotNull();
        assertThat(savedFallback.getCreated()).isNotNull();
        assertThat(savedFallback.getCreatedFrom()).describedAs("Created by system means always null because no user involved").isNull();
        assertThat(savedFallback.getId()).describedAs("Fallback is created as first entry when none exists, will always use the first possible one").isEqualTo(0);
        assertThat(savedFallback.getPasswordSourceType()).isEqualTo(CipherPasswordSourceType.NONE);
        assertThat(savedFallback.getPasswordSourceData()).isNull();
        assertThat(savedFallback.getTestEncrypted()).isEqualTo("fallback".getBytes(Charset.forName("UTF-8")));
        assertThat(savedFallback.getTestInitialVector()).isNull();
        assertThat(savedFallback.getTestText()).isEqualTo("fallback");


    }

    @Test
    void provider_returns_found_data_without_fallback_when_data_available() {

        ScheduleCipherPoolData data1 = mock(ScheduleCipherPoolData.class);
        ScheduleCipherPoolData data2 = mock(ScheduleCipherPoolData.class);

        /* prepare */
        when(repository.findAll()).thenReturn(List.of(data1, data2));

        /* execute */
        List<ScheduleCipherPoolData> result = providerToTest.ensurePoolDataAvailable();

        /* test */
        assertThat(result).hasSize(2).contains(data1, data2);

    }

}
