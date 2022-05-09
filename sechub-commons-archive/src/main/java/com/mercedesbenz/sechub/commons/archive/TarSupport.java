package com.mercedesbenz.sechub.commons.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.archive.ArchivePathInspector.ArchivePathInspectionResult;

public class TarSupport {
    private static final NoInspectorFallbackInspectionResult NO_FILTER_RESULT = new NoInspectorFallbackInspectionResult();
    private static final Logger LOG = LoggerFactory.getLogger(TarSupport.class);

    public void extractTar(InputStream inputStream, String identifier, File outputDir) throws IOException {
        extractTar(inputStream, identifier, outputDir, null);
    }

    public void extractTar(InputStream inputStream, String identifier, File outputDir, ArchivePathInspector inspector) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("tar", inputStream)) {

            extract(archiveInputStream, identifier, outputDir, inspector);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to extract archive:" + identifier + " at " + outputDir, e);
        }

    }

    private void extract(ArchiveInputStream archiveInputStream, String identifier, File outputDir, ArchivePathInspector inspector)
            throws ArchiveException, IOException {
        if (!(archiveInputStream instanceof TarArchiveInputStream)) {
            throw new IOException("Cannot extract: " + identifier + " because it is not a tar archive");
        }

        TarArchiveInputStream tarArchiveInputStream = (TarArchiveInputStream) archiveInputStream;
        TarArchiveEntry entry = null;
        while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null) {

            ArchivePathInspectionResult result = startInspection(inspector, entry);
            if (result == null) {
                continue;
            }
            if (!result.isAccepted()) {
                continue;
            }
            String name = null;

            if (result.isPathChangeWanted()) {
                name = result.getWantedPath();
            } else {
                name = entry.getName();
            }
            File outputFile = new File(outputDir, name);

            if (entry.isDirectory()) {
                LOG.debug("Write output directory {}.", outputFile.getAbsolutePath());
                if (!outputFile.exists()) {
                    if (!outputFile.mkdirs()) {
                        throw new IOException("Was not able to create directory:" + outputFile.getAbsolutePath());
                    }
                }
            } else {
                LOG.info("Creating output file: ", outputFile.getAbsolutePath());
                try (OutputStream outputFileStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(tarArchiveInputStream, outputFileStream);
                }
            }
        }
    }

    private static class NoInspectorFallbackInspectionResult extends ArchivePathInspectionResult {
        @Override
        public boolean isPathChangeWanted() {
            return false; // never
        }

        @Override
        public boolean isAccepted() {
            return true; // always
        }
    }

    private ArchivePathInspectionResult startInspection(ArchivePathInspector filter, TarArchiveEntry entry) {
        if (filter == null) {
            return NO_FILTER_RESULT;
        }
        return filter.inspect(entry.getName());
    }

}
