// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ArchiveSafeguardTest {

    final List<File> tempFiles = new ArrayList<>();

    @AfterEach
    void tearDown() throws IOException {
        for (File file : tempFiles) {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    void new_archive_safeguard_object_has_expected_properties() throws IOException {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("10KB");
        long maxEntries = 10L;
        FileSize entryFileSize = new FileSize("1KB");
        long maxDirectoryDepth = 10L;
        Duration timeout = Duration.ofSeconds(10);

        ArchiveSafeguardProperties archiveSafeguardProperties = new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);
        ArchiveInputStream<?> zipArchiveInputStream = createZipArchiveInputStream(maxEntries, entryFileSize, maxDirectoryDepth);

        /* execute */
        ArchiveSafeguard result = new ArchiveSafeguard(zipArchiveInputStream, archiveSafeguardProperties);

        /* test */
        assertThat(result.getArchiveInputStream(), notNullValue());
        assertThat(result.getProperties(), is(archiveSafeguardProperties));
        assertThat(result.getEntriesCount(), is(0L));
        assertThat(result.getStartTime(), nullValue());
    }

    @ParameterizedTest
    @ArgumentsSource(ArchiveSafeguardNullArgumentsProvider.class)
    void new_archive_safeguard_object_does_not_allow_null_arguments(ArchiveInputStream<?> archiveInputStream, ArchiveSafeguardProperties archiveSafeguardProperties, String argumentName) {
        /* execute */
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new ArchiveSafeguard(archiveInputStream, archiveSafeguardProperties));

        /* test */
        assertThat(exception.getMessage(), is("Property %s must not be null".formatted(argumentName)));
    }

    @Test
    void get_next_entry_with_valid_archive_works() throws IOException {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("10KB");
        long maxEntries = 10L;
        FileSize entryFileSize = new FileSize("1KB");
        long maxDirectoryDepth = 10L;
        Duration timeout = Duration.ofSeconds(10);
        ArchiveInputStream<?> archiveInputStream = createZipArchiveInputStream(maxEntries - 1, entryFileSize, maxDirectoryDepth - 1);
        ArchiveSafeguardProperties archiveSafeguardProperties = new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);
        ArchiveSafeguard safeguard = new ArchiveSafeguard(archiveInputStream, archiveSafeguardProperties);

        /* execute */
        assertDoesNotThrow(() -> {
            ArchiveEntry entry;
            while ((entry = safeguard.getNextEntry()) != null) {
                assertThat(entry, notNullValue());
            }
        }, "getNextEntry should not throw any exceptions");
    }

    @Test
    void get_next_entry_with_max_file_size_uncompressed_exceeded_throws_exception() throws IOException {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("10KB");
        long maxEntries = 10;
        long maxDirectoryDepth = 1L;
        Duration timeout = Duration.ofSeconds(10);
        ArchiveSafeguardProperties archiveSafeguardProperties = new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);
        FileSize entryFileSize = new FileSize("1KB");
        ArchiveInputStream<?> zipArchiveInputStream = createZipArchiveInputStream(maxEntries, entryFileSize, maxDirectoryDepth);
        ArchiveSafeguard safeguard = new ArchiveSafeguard(zipArchiveInputStream, archiveSafeguardProperties);

        /* execute & test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ArchiveEntry entry;
            while ((entry = safeguard.getNextEntry()) != null) {
                assertThat(entry, notNullValue());
                if (entry.isDirectory()) {
                    // If it's a directory, create it
                    File dir = new File(entry.getName());
                    tempFiles.add(dir);
                    dir.mkdirs();
                } else {
                    // If it's a file, write its contents to a new file
                    File file = File.createTempFile("tempfile", ".tmp");
                    tempFiles.add(file);
                    FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.copy(safeguard.getArchiveInputStream(), fos);
                    fos.close();
                    String x = "";
                }
            }
        });
        assertThat(exception.getMessage(), is("File size exceeds the maximum allowed value of %s".formatted(maxFileSizeUncompressed.getSizeString())));
    }

    @Test
    void get_next_entry_with_too_many_entries_throws_exception() throws IOException {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("1MB");
        long maxEntries = 10L;
        long maxDirectoryDepth = 1L;
        Duration timeout = Duration.ofSeconds(10);
        ArchiveSafeguardProperties archiveSafeguardProperties = new ArchiveSafeguardProperties(maxFileSizeUncompressed, maxEntries, maxDirectoryDepth, timeout);
        FileSize entryFileSize = new FileSize("1KB");
        ArchiveInputStream<?> archiveInputStream = createZipArchiveInputStream(maxEntries + 1, entryFileSize, maxDirectoryDepth);
        ArchiveSafeguard safeguard = new ArchiveSafeguard(archiveInputStream, archiveSafeguardProperties);

        /* execute & test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ArchiveEntry entry;
            while ((entry = safeguard.getNextEntry()) != null) {
                assertThat(entry, notNullValue());
            }
        });
        assertThat(exception.getMessage(), is("Number of entries exceeds the maximum allowed value of %s".formatted(maxEntries)));
    }

    private ZipArchiveInputStream createZipArchiveInputStream(long entriesCount, FileSize entryFileSize, long maxDirectoryDepth) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("archive.zip");
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos)) {

            // Create nested directories
            String directoryPath = "";
            for (int i = 1; i <= maxDirectoryDepth; i++) {
                directoryPath += "dir" + i + "/";
                ZipArchiveEntry dirEntry = new ZipArchiveEntry(directoryPath);
                zaos.putArchiveEntry(dirEntry);
                zaos.closeArchiveEntry();
            }

            // Create temp files in the deepest directory
            byte[] entryData = new byte[(int) entryFileSize.getBytes()];
            for (int i = 1; i <= entriesCount; i++) {
                ZipArchiveEntry entry = new ZipArchiveEntry(directoryPath + "file" + i + ".txt");
                zaos.putArchiveEntry(entry);
                zaos.write(entryData);
                zaos.closeArchiveEntry();
            }

            zaos.close();
            byte[] bytes = Files.readAllBytes(new File("archive.zip").toPath());
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return new ZipArchiveInputStream(bais);
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private static class ArchiveSafeguardNullArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(null, new ArchiveSafeguardProperties(new FileSize("100MB"), 100L, 10L, Duration.ofSeconds(10)), "archiveInputStream"),
                    Arguments.of(new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0])), null, "archiveSafeguardProperties")
            );
        }
    }
}
