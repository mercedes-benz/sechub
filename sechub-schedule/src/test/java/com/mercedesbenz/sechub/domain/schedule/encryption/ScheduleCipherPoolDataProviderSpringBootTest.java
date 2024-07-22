// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;

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

        assertThat(savedFallback.getAlgorithm()).isEqualTo(SecHubCipherAlgorithm.NONE);
        assertThat(savedFallback.getTestEncrypted()).isNotNull();
        assertThat(savedFallback.getCreated()).isNotNull();
        assertThat(savedFallback.getCreatedFrom()).describedAs("Created by system means always null because no user involved").isNull();
        assertThat(savedFallback.getId()).describedAs("Fallback is created as first entry when none exists, will always use the first possible one")
                .isEqualTo(0);
        assertThat(savedFallback.getPasswordSourceType()).isEqualTo(SecHubCipherPasswordSourceType.NONE);
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

    @Test
    void isContainingExactlyGivenPoolIds_null_throws_illegal_argument() {

        /* execute +test */
        assertThatThrownBy(() -> providerToTest.isContainingExactlyGivenPoolIds(null)).isInstanceOf(IllegalArgumentException.class);

    }

    @ParameterizedTest
    @ArgumentsSource(SimplePoolIdSetArgumentsProvider.class)
    void isContainingExactlyGivenPoolIds_contains_same_as_expected(Set<Long> poolIdsInDbAndCurrentlyInMemory) {

        /* prepare */
        when(repository.fetchAllCipherPoolIds()).thenReturn(poolIdsInDbAndCurrentlyInMemory);

        /* execute */
        boolean result = providerToTest.isContainingExactlyGivenPoolIds(poolIdsInDbAndCurrentlyInMemory);

        /* test */
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(DifferentPoolIdSetArgumentsProvider.class)
    void isContainingExactlyGivenPoolIds_not_same(Set<Long> currentPoolIds, Set<Long> poolIdsInDatabase) {

        /* prepare */
        when(repository.fetchAllCipherPoolIds()).thenReturn(poolIdsInDatabase);

        /* execute */
        boolean result = providerToTest.isContainingExactlyGivenPoolIds(currentPoolIds);

        /* test */
        assertThat(result).isFalse();
    }

    static class SimplePoolIdSetArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(Arguments.of(Set.of()), Arguments.of(Set.of(0L)), Arguments.of(Set.of(0L, 1L, 2L)), Arguments.of(Set.of(0L, 1L, 2L, 3L, 5L)),
                    Arguments.of(Set.of(3L, 5L)));
        }

    }

    static class DifferentPoolIdSetArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(Arguments.of(Set.of(), Set.of(1L)), Arguments.of(Set.of(0L), Set.of()), Arguments.of(Set.of(0L), Set.of(1L)),
                    Arguments.of(Set.of(0L, 1L, 2L), Set.of(0L, 1l)), Arguments.of(Set.of(0L, 1L, 2L, 3L, 5L), Set.of(0L, 1L, 2L, 3L, 6L)),
                    Arguments.of(Set.of(3L, 5L), Set.of(5L, 6L)));
        }

    }

}
