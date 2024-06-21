// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

class SafeArchiveInputStreamTest {

    private static final FileSize DEFAULT_MAX_FILE_SIZE_UNCOMPRESSED = new FileSize("10KB");
    private static final long DEFAULT_MAX_ENTRIES = 10L;
    private static final FileSize DEFAULT_ENTRY_FILE_SIZE = new FileSize("1KB");
    private static final long DEFAULT_MAX_DIRECTORY_DEPTH = 10L;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10L);

    final List<File> tempFiles = new ArrayList<>();

    @AfterEach
    void tearDown() throws IOException {
        for (File file : tempFiles) {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    void new_safe_archive_input_stream_has_expected_properties() throws IOException {
        /* prepare */
        /* @formatter:off */
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(
                DEFAULT_MAX_FILE_SIZE_UNCOMPRESSED,
                DEFAULT_MAX_ENTRIES,
                DEFAULT_MAX_DIRECTORY_DEPTH,
                DEFAULT_TIMEOUT
        );
        /* @formatter:on */
        ArchiveInputStream<?> archiveInputStream = createZipArchiveInputStream(DEFAULT_MAX_ENTRIES, DEFAULT_ENTRY_FILE_SIZE, DEFAULT_MAX_DIRECTORY_DEPTH);

        /* execute */
        SafeArchiveInputStream result = new SafeArchiveInputStream(archiveInputStream, archiveExtractionConstraints);

        /* test */
        assertThat(result.getArchiveExtractionConstraints(), is(archiveExtractionConstraints));
        assertThat(result.getStartTime(), nullValue());
        assertThat(result.getEntriesCount(), is(0L));
        assertThat(result.getBytesRead(), is(0L));
    }

    @ParameterizedTest
    @ArgumentsSource(NullArgumentsProvider.class)
    void new_safe_archive_input_stream_does_not_allow_null_arguments(ArchiveInputStream<?> archiveInputStream,
            ArchiveExtractionConstraints archiveExtractionConstraints, String argumentName) {
        /* execute */
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new SafeArchiveInputStream(archiveInputStream, archiveExtractionConstraints));

        /* test */
        assertThat(exception.getMessage(), is("Property %s must not be null".formatted(argumentName)));
    }

    /**
     * This test case creates an archive with 10 entries of 1KB each. The input
     * stream allows 10 entries with a maximum uncompressed size of 1KB each.
     * Therefore, the extraction should work without any exceptions.
     */
    @Test
    void get_next_entry_with_valid_archive_works() throws IOException {
        /* prepare */
        ArchiveInputStream<?> zipArchiveInputStream = createZipArchiveInputStream(DEFAULT_MAX_ENTRIES, DEFAULT_ENTRY_FILE_SIZE, DEFAULT_MAX_DIRECTORY_DEPTH);
        /* @formatter:off */
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(
                DEFAULT_MAX_FILE_SIZE_UNCOMPRESSED,
                DEFAULT_MAX_ENTRIES,
                DEFAULT_MAX_DIRECTORY_DEPTH,
                DEFAULT_TIMEOUT
        );
        /* @formatter:on */
        SafeArchiveInputStream safeArchiveInputStream = new SafeArchiveInputStream(zipArchiveInputStream, archiveExtractionConstraints);

        /* execute & test */
        assertDoesNotThrow(() -> readAllArchiveEntriesFromStream(safeArchiveInputStream), "getNextEntry should not throw any exceptions");
        assertThat(safeArchiveInputStream.getEntriesCount(), is(DEFAULT_MAX_ENTRIES));
        assertThat(safeArchiveInputStream.getBytesRead(), is(DEFAULT_MAX_FILE_SIZE_UNCOMPRESSED.getBytes()));
    }

    @Test
    void get_next_entry_with_timeout_exceeded_throws_exception() throws IOException {
        /* prepare */
        Duration timeout = Duration.ofMillis(1L);
        /* @formatter:off */
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(
                DEFAULT_MAX_FILE_SIZE_UNCOMPRESSED,
                DEFAULT_MAX_ENTRIES,
                DEFAULT_MAX_DIRECTORY_DEPTH,
                timeout
        );
        /* @formatter:on */
        ArchiveInputStream<?> zipArchiveInputStream = createZipArchiveInputStream(DEFAULT_MAX_ENTRIES, DEFAULT_ENTRY_FILE_SIZE, DEFAULT_MAX_DIRECTORY_DEPTH);
        SafeArchiveInputStream safeArchiveInputStream = new SafeArchiveInputStream(zipArchiveInputStream, archiveExtractionConstraints);

        /* execute & test */
        ArchiveExtractionException exception = assertThrows(ArchiveExtractionException.class, () -> readAllArchiveEntriesFromStream(safeArchiveInputStream));
        assertThat(exception.getMessage(), is("Timeout exceeded"));
    }

    /**
     * This test case creates an archive with 10 entries of 1KB each. The maximum
     * allowed file size is 9KB. Therefore, the extraction of the 10th entry should
     * throw an exception.
     */
    @Test
    void get_next_entry_with_max_file_size_uncompressed_exceeded_throws_exception() throws IOException {
        /* prepare */
        FileSize maxFileSizeUncompressed = new FileSize("9KB");
        /* @formatter:off */
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(
                maxFileSizeUncompressed,
                DEFAULT_MAX_ENTRIES,
                DEFAULT_MAX_DIRECTORY_DEPTH,
                DEFAULT_TIMEOUT
        );
        /* @formatter:on */
        ArchiveInputStream<?> zipArchiveInputStream = createZipArchiveInputStream(DEFAULT_MAX_ENTRIES, DEFAULT_ENTRY_FILE_SIZE, DEFAULT_MAX_DIRECTORY_DEPTH);
        SafeArchiveInputStream safeArchiveInputStream = new SafeArchiveInputStream(zipArchiveInputStream, archiveExtractionConstraints);

        /* execute & test */
        ArchiveExtractionException exception = assertThrows(ArchiveExtractionException.class, () -> readAllArchiveEntriesFromStream(safeArchiveInputStream));
        assertThat(exception.getMessage(), is("File size exceeds the maximum allowed value of %s".formatted(maxFileSizeUncompressed.getSizeString())));
        assertThat(safeArchiveInputStream.getEntriesCount(), is(DEFAULT_MAX_ENTRIES));
        assertThat(safeArchiveInputStream.getBytesRead(), is(maxFileSizeUncompressed.getBytes() + DEFAULT_ENTRY_FILE_SIZE.getBytes()));
    }

    /**
     * This test case creates an archive with 11 entries. The maximum allowed number
     * of entries is 10. Therefore, the extraction of the 11th entry should throw an
     * exception.
     */
    @Test
    void get_next_entry_with_too_many_entries_throws_exception() throws IOException {
        /* prepare */
        /* @formatter:off */
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(
                DEFAULT_MAX_FILE_SIZE_UNCOMPRESSED,
                DEFAULT_MAX_ENTRIES,
                DEFAULT_MAX_DIRECTORY_DEPTH,
                DEFAULT_TIMEOUT
        );
        /* @formatter:on */
        long entriesCount = DEFAULT_MAX_ENTRIES + 1;
        ArchiveInputStream<?> zipArchiveInputStream = createZipArchiveInputStream(entriesCount, DEFAULT_ENTRY_FILE_SIZE, DEFAULT_MAX_DIRECTORY_DEPTH);
        SafeArchiveInputStream safeArchiveInputStream = new SafeArchiveInputStream(zipArchiveInputStream, archiveExtractionConstraints);

        /* execute & test */
        ArchiveExtractionException exception = assertThrows(ArchiveExtractionException.class, () -> readAllArchiveEntriesFromStream(safeArchiveInputStream));
        assertThat(exception.getMessage(), is("Number of entries exceeds the maximum allowed value of %s".formatted(DEFAULT_MAX_ENTRIES)));
    }

    /**
     * This test case creates an archive with a single entry. The entry has a
     * directory depth of 11. The maximum allowed directory depth is 10. Therefore,
     * the extraction of the entry should throw an exception.
     */
    @Test
    void get_next_entry_with_too_many_directories_throws_exception() throws IOException {
        /* prepare */
        /* @formatter:off */
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(
                DEFAULT_MAX_FILE_SIZE_UNCOMPRESSED,
                DEFAULT_MAX_ENTRIES,
                DEFAULT_MAX_DIRECTORY_DEPTH,
                DEFAULT_TIMEOUT
        );
        /* @formatter:on */
        long directoriesCount = DEFAULT_MAX_DIRECTORY_DEPTH + 1;
        ArchiveInputStream<?> zipArchiveInputStream = createZipArchiveInputStream(DEFAULT_MAX_ENTRIES, DEFAULT_ENTRY_FILE_SIZE, directoriesCount);
        SafeArchiveInputStream safeArchiveInputStream = new SafeArchiveInputStream(zipArchiveInputStream, archiveExtractionConstraints);

        /* execute & test */
        ArchiveExtractionException exception = assertThrows(ArchiveExtractionException.class, () -> readAllArchiveEntriesFromStream(safeArchiveInputStream));
        assertThat(exception.getMessage(), is("Directory depth exceeds the maximum allowed value of %s".formatted(DEFAULT_MAX_DIRECTORY_DEPTH)));
    }

    private ZipArchiveInputStream createZipArchiveInputStream(long entriesCount, FileSize entryFileSize, long maxDirectoryDepth) throws IOException {
        File file = File.createTempFile("archive", ".zip");
        tempFiles.add(file);
        try (FileOutputStream fos = new FileOutputStream(file);
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
            byte[] bytes = Files.readAllBytes(file.toPath());
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return new ZipArchiveInputStream(bais);
        }
    }

    private void readAllArchiveEntriesFromStream(SafeArchiveInputStream safeArchiveInputStream) throws IOException {
        ArchiveEntry entry;
        while ((entry = safeArchiveInputStream.getNextEntry()) != null) {
            assertThat(entry, notNullValue());
            // simulate the extraction of the archive
            // otherwise no bytes are read from the input stream
            extractData(entry, safeArchiveInputStream);
        }
    }

    private void extractData(ArchiveEntry entry, InputStream inputStream) throws IOException {
        if (!entry.isDirectory()) {
            // If it's a file, write its contents to a new file
            File file = File.createTempFile("tempfile", ".tmp");
            tempFiles.add(file);
            FileOutputStream fos = new FileOutputStream(file);
            IOUtils.copy(inputStream, fos);
            fos.close();
        }
    }

    private static class NullArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of(null, new ArchiveExtractionConstraints(new FileSize("100MB"), 100L, 10L, Duration.ofSeconds(10)), "archiveInputStream"),
                    Arguments.of(new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0])), null, "archiveExtractionConstraints")
            );
            /* @formatter:on */
        }
    }
}
