package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.commons.encryption.PersistentCipherType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;

class ScheduleCipherAlgorithmTest {

    /*
     * We have separated the database enumeration from the encryption parts - for
     * different reasons. The test will ensure that the types are as expected.
     */
    @ParameterizedTest
    @ArgumentsSource(CipherAlgorithmTestData.class)
    void databaseAlgorithmHasExpectedInternalType(SecHubCipherAlgorithm algorithmInDb, PersistentCipherType internalType) {
        assertThat(algorithmInDb.getType()).isEqualTo(internalType);
    }

    private static class CipherAlgorithmTestData implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            /* @formatter:off */
            return Stream.of(
                            Arguments.of(SecHubCipherAlgorithm.NONE, PersistentCipherType.NONE),
                            Arguments.of(SecHubCipherAlgorithm.AES_GCM_SIV_128, PersistentCipherType.AES_GCM_SIV_128),
                            Arguments.of(SecHubCipherAlgorithm.AES_GCM_SIV_256, PersistentCipherType.AES_GCM_SIV_256)
                        )
                    ;
            /* @formatter:on */
        }

    }
}
