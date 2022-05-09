package com.mercedesbenz.sechub.commons.archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ZipSupport.class);

    public boolean isZipFile(Path pathToFile) {
        try (ZipFile zipFile = new ZipFile(pathToFile.toFile())) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isZipFileStream(InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            boolean isZipped = zis.getNextEntry() != null;
            return isZipped;
        } catch (IOException e) {
            // only interesting for debugging - normally it is just no ZIP file.
            LOG.debug("The zip file check did fail", e);
            return false;
        }
    }

}
