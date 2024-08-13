// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to write text to files. Will always use UTF-8.
 *
 * @author Albert Tregnaghi
 *
 */
public class TextFileWriter {

    private static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");
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
    public void writeTextToFile(File targetFile, String text, boolean overwrite) throws IOException {
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
            if (!parentFile.exists()) {
                // we use new method - in case of a problem it throws an IO exception with
                // details
                Files.createDirectories(parentFile.toPath());
            }
            if (!targetFile.createNewFile()) {
                throw new IllegalStateException("was not able to create new file:" + targetFile);
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), CHARSET_UTF_8))) {
            bw.write(text);
        }

        LOG.debug("Written: {}", targetFile);
    }

}
