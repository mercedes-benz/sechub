// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ArchiveSafeguardTest {

    @Test
    void new_archive_safeguard_object_has_expected_properties() throws IOException {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("100MB");
        long maxEntries = 100L;
        long maxDirectoryDepth = 10L;
        Duration timeout = Duration.ofSeconds(10);

        ArchiveSafeguardProperties properties = new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);
        ZipArchiveInputStream inputStream = createDummyZipArchiveInputStream();

        /* execute */
        ArchiveSafeguard result = new ArchiveSafeguard(inputStream, properties);

        /* test */
        assertThat(result.getInputStream(), is(inputStream));
        assertThat(result.getProperties(), is(properties));
        assertThat(result.getEntriesCount(), is(0L));
        assertThat(result.getDirectoryDepthCount(), is(0L));
        assertThat(result.getDirectoryDepthCount(), is(0L));
        assertThat(result.getStartTime(), nullValue());
    }

    @ParameterizedTest
    @ArgumentsSource(ArchiveSafeguardNullArgumentsProvider.class)
    void new_archive_safeguard_object_does_not_allow_null_arguments(ZipArchiveInputStream inputStream, ArchiveSafeguardProperties properties, String argumentName) {
        /* execute */
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new ArchiveSafeguard(inputStream, properties));

        /* test */
        assertThat(exception.getMessage(), is("Property %s must not be null".formatted(argumentName)));
    }

    @Test
    void get_next_entry_with_valid_archive_works() throws IOException {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("100MB");
        long maxEntries = 100L;
        long maxDirectoryDepth = 10L;
        Duration timeout = Duration.ofSeconds(10);
        ArchiveSafeguardProperties properties = new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);
        ZipArchiveInputStream inputStream = createDummyZipArchiveInputStream();
        ArchiveSafeguard safeguard = new ArchiveSafeguard(inputStream, properties);

        /* execute */
        assertDoesNotThrow(() -> {
            ArchiveEntry entry;
            while ((entry = safeguard.getNextEntry()) != null) {
                assertThat(entry, notNullValue());
            }
        }, "getNextEntry should not throw any exceptions");
    }

    private ZipArchiveInputStream createDummyZipArchiveInputStream() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        // Add an entry
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write("Test data".getBytes());
        zipOutputStream.closeEntry();

        // Close the ZipOutputStream
        zipOutputStream.close();

        // Create the ByteArrayInputStream and ZipArchiveInputStream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return new ZipArchiveInputStream(byteArrayInputStream);
    }

    private static class ArchiveSafeguardNullArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(null, new ArchiveSafeguardProperties(new FileSize("100MB"), 100L, 10L, Duration.ofSeconds(10)), "inputStream"),
                    Arguments.of(new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0])), null, "properties")
            );
        }
    }
}
