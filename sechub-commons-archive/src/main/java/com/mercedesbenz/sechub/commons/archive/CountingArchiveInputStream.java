package com.mercedesbenz.sechub.commons.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.CountingInputStream;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

class CountingArchiveInputStream {

    private final ArchiveInputStream<?> archiveInputStream;
    private final CountingInputStream countingInputStream;

    CountingArchiveInputStream(ArchiveSupport.ArchiveType archiveType, InputStream inputStream) {
        requireNonNull(inputStream, "Property inputStream must not be null");
        countingInputStream = new CountingInputStream(inputStream);
        requireNonNull(archiveType, "Property archiveType must not be null" );
        try {
            archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(archiveType.getType(), countingInputStream);
        } catch (ArchiveException e) {
            throw new RuntimeException(e);
        }
    }

    ArchiveInputStream<?> getArchiveInputStream() {
        return archiveInputStream;
    }

    ArchiveEntry getNextEntry() throws IOException {
        return archiveInputStream.getNextEntry();
    }

    long getBytesRead() {
        return countingInputStream.getBytesRead();
    }
}
