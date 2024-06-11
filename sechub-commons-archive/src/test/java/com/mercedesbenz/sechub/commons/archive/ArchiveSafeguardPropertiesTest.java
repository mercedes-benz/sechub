// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

class ArchiveSafeguardPropertiesTest {

    @Test
    void new_archive_safeguard_properties_object_has_expected_properties() {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("100MB");
        long maxEntries = 100L;
        long maxDirectoryDepth = 10L;
        Duration timeout = Duration.ofSeconds(10);

        /* execute */
        ArchiveSafeguardProperties result = new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);

        /* test */
        assertThat(result.getMaxFileSizeUncompressed(), is(maxFileSizeUncompressed));
        assertThat(result.getMaxEntries(), is(maxEntries));
        assertThat(result.getMaxDirectoryDepth(), is(maxDirectoryDepth));
        assertThat(result.getTimeout(), is(timeout));
    }

    @ParameterizedTest
    @ArgumentsSource(ArchiveExtractionNullArgumentsProvider.class)
    void new_archive_safeguard_properties_object_does_not_allow_null_arguments(FileSize maxFileSizeUncompressed,
                                                                               Duration timeout,
                                                                               String nullArgumentName) {
        /* execute */
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new ArchiveSafeguardProperties(maxFileSizeUncompressed, 100L, 10L, timeout));

        /* test */
        assertThat(exception.getMessage(), is("Property %s must not be null".formatted(nullArgumentName)));
    }

    @ParameterizedTest
    @ArgumentsSource(ArchiveExtractionInvalidArgumentsProvider.class)
    void new_archive_safeguard_properties_object_does_not_allow_invalid_arguments(long maxEntries,
                                                                                   long maxDirectoryDepth,
                                                                                   Duration timeout,
                                                                                   String invalidArgumentName) {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("100MB");

        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout));

        /* test */
        assertThat(exception.getMessage(), is("Property %s must be greater than 0".formatted(invalidArgumentName)));
    }

    private static class ArchiveExtractionNullArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(null, Duration.ofSeconds(10), "maxFileSizeUncompressed"),
                    Arguments.of(new FileSize("100MB"), null, "timeout")
            );
        }
    }

    private static class ArchiveExtractionInvalidArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(-1L, 10L, Duration.ofSeconds(10), "maxEntries"),
                    Arguments.of(0L, 10L, Duration.ofSeconds(10), "maxEntries"),
                    Arguments.of(100L, -1L, Duration.ofSeconds(10), "maxDirectoryDepth"),
                    Arguments.of(100L, 0L, Duration.ofSeconds(10), "maxDirectoryDepth"),
                    Arguments.of(100L, 10L, Duration.ofSeconds(-1), "timeout"),
                    Arguments.of(100L, 10L, Duration.ofSeconds(0), "timeout")
            );
        }
    }
}
