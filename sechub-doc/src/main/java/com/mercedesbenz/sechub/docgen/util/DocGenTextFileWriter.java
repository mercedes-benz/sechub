// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.docgen.GeneratorConstants;

public class DocGenTextFileWriter {

    private static final Logger LOG = LoggerFactory.getLogger(DocGenTextFileWriter.class);

    private LicenseHeaderProvider licenseHeaderProvider;

    public DocGenTextFileWriter() {
        licenseHeaderProvider = new LicenseHeaderProvider();
    }

    /**
     * Save text file, does overwrite existing ones! Adds missing headers
     *
     * @param targetFile
     * @param origin
     * @throws IOException
     */
    public void writeTextToFile(File targetFile, String origin) throws IOException {
        writeTextToFile(targetFile, origin, true);
    }

    /**
     * Save text file, does overwrite existing ones (when wanted only! Adds missing
     * headers when writing and header is necessary
     *
     * @param targetFile
     * @param origin
     * @param overwrite
     * @throws IOException
     */
    public void writeTextToFile(File targetFile, String origin, boolean overwrite) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("null not allowed as file!");
        }
        String licenseHeader = licenseHeaderProvider.getLicenseHeader(origin, targetFile);
        String text;
        if (licenseHeader == null) {
            text = origin;
        } else {
            text = licenseHeader + "\n" + origin;
        }

        if (targetFile.exists()) {
            if (!overwrite) {
                LOG.warn("Already existing and 'overwrite' not enabled:" + targetFile);
                return;
            }
            /*
             * Use old API and not Files.delete(..) - reason: I want not to accidently
             * delete a folder! With old API it is ensured this is only a file not a dir
             */
            if (/* NOSONAR */!targetFile.delete()) {
                throw new IOException("was not able to delete existing file:" + targetFile);
            }
        }

        if (!targetFile.exists()) {
            File parentFile = targetFile.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new IllegalStateException("Not able to create folder structure for:" + targetFile);
            }
            if (!targetFile.createNewFile()) {
                throw new IllegalStateException("was not able to create new file:" + targetFile);
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
            bw.write(text);
        }
        if (GeneratorConstants.DEBUG) {
            LOG.info("Written:" + targetFile);
        }
    }
}
