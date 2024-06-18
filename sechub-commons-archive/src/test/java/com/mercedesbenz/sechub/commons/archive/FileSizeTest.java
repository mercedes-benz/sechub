// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

class FileSizeTest {

    @ParameterizedTest
    @ArgumentsSource(FileSizeArgumentsProvider.class)
    void new_file_size_object_has_expected_size_str_and_size_bytes(String sizeStr, long sizeBytes) {
        /* execute */
        FileSize result = new FileSize(sizeStr);

        /* test */
        assertThat(result.getSizeString(), is(sizeStr.toUpperCase()));
        assertThat(result.getBytes(), is(sizeBytes));
    }

    @Test
    void new_file_size_object_does_not_allow_null_size_str() {
        /* execute */
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new FileSize(null));

        /* test */
        assertThat(exception.getMessage(), is("sizeStr property must not be null"));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "10", "10b", "10mbb", "10gbg", "10GiB", "10tb", "10TB" })
    void new_file_size_object_does_not_allow_invalid_size_str(String sizeStr) {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new FileSize(sizeStr));

        /* test */
        assertThat(exception.getMessage(), is("Invalid file size %s.".formatted(sizeStr.toUpperCase())));
    }

    private static class FileSizeArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(Arguments.of("10kb", 10240L), Arguments.of("10KB", 10240L), Arguments.of("10mb", 10485760L), Arguments.of("10MB", 10485760L),
                    Arguments.of("10gb", 10737418240L), Arguments.of("10GB", 10737418240L));
        }
    }
}
