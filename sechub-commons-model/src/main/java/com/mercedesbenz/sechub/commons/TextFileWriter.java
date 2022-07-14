// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextFileWriter {

    private static final Logger LOG = LoggerFactory.getLogger(TextFileWriter.class);

    /**
     * Saves a given text to a target file. If the parent folder structure does not
     * exist, it will be automatically created.
     *
     * @param targetFile the target file
     * @param text       content to write
     * @param overwrite  when <code>true</code>, existing files will be deleted
     *                   before write
     * @throws IOException
     */
    public void save(File targetFile, String text, boolean overwrite) throws IOException {
        internalSave(targetFile, text, overwrite, Charset.forName("UTF-8"));
    }

    private void internalSave(File targetFile, String text, boolean overwrite, Charset charset) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("null not allowed as file!");
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
            if (!targetFile.delete()) {
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
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), charset))) {
            bw.write(text);
        }

        LOG.debug("Written: {}", targetFile);
    }

}
