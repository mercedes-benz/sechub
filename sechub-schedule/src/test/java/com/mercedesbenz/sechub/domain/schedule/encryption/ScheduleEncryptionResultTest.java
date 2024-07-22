// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.commons.encryption.EncryptionResult;
import com.mercedesbenz.sechub.commons.encryption.InitializationVector;

class ScheduleEncryptionResultTest {

    @ParameterizedTest
    @ArgumentsSource(IllegalSchedulerEncryptionResultParameters.class)
    void constructor_with_invalid_arguments_throws_illegal_argument_exception(Long poolId, EncryptionResult encryptionResult) {

        assertThatThrownBy(() -> new ScheduleEncryptionResult(poolId, encryptionResult)).isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void enryption_result_with_valid_parameters_contains_the_information() {
        Long poolId = Long.valueOf(0);
        EncryptionResult encryptionResult = createValidEncryptionResult();

        /* execute */
        ScheduleEncryptionResult scheduleEncryptionResult = new ScheduleEncryptionResult(poolId, encryptionResult);

        /* test */
        assertThat(scheduleEncryptionResult).isNotNull();
        assertThat(scheduleEncryptionResult.getEncryptedData()).isEqualTo(encryptionResult.getEncryptedData());
        assertThat(scheduleEncryptionResult.getInitialVector()).isEqualTo(encryptionResult.getInitialVector());
        assertThat(scheduleEncryptionResult.getCipherPoolId()).isEqualTo(poolId);

    }

    private static class IllegalSchedulerEncryptionResultParameters implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(Arguments.of(null, null), Arguments.of(Long.valueOf(0), null), Arguments.of(null, createValidEncryptionResult())

            );
        }

    }

    private static EncryptionResult createValidEncryptionResult() {
        return new EncryptionResult("xxx".getBytes(), new InitializationVector("vector".getBytes()));
    }

}
