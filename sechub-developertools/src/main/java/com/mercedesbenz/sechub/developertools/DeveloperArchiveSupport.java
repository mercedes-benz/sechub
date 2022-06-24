// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class DeveloperArchiveSupport {

    public void compressToTar(File folder, File targetFile) throws IOException {
        ArchiveStreamFactory asf = new ArchiveStreamFactory();

        try (FileOutputStream fos = new FileOutputStream(targetFile);
                TarArchiveOutputStream tarOutputStream = (TarArchiveOutputStream) asf.createArchiveOutputStream(ArchiveStreamFactory.TAR, fos)) {

            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

            compressFilesRecursively(folder.getAbsolutePath(), folder, tarOutputStream);
            tarOutputStream.finish();

        } catch (Exception e) {
            throw new IOException("Was not able to append folder: " + folder + " to tar:" + targetFile, e);
        }

    }

    public void compressToZip(File folder, File targetFile) throws IOException {
        ArchiveStreamFactory asf = new ArchiveStreamFactory();

        try (FileOutputStream fos = new FileOutputStream(targetFile);
                ArchiveOutputStream archiveOutputStram = asf.createArchiveOutputStream(ArchiveStreamFactory.ZIP, fos)) {
            compressFilesRecursively(folder.getAbsolutePath(), folder, archiveOutputStram);
            archiveOutputStram.finish();
        } catch (Exception e) {
            throw new IOException("Was not able to append folder: " + folder + " to zip: " + targetFile, e);
        }

    }

    private void compressFilesRecursively(String baseFolderPath, File file, ArchiveOutputStream aos) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                compressFilesRecursively(baseFolderPath, child, aos);
            }
        } else {

            ArchiveEntry archiveEntry = aos.createArchiveEntry(file, entryName(baseFolderPath, file));

            aos.putArchiveEntry(archiveEntry);

            IOUtils.copy(new FileInputStream(file), aos);

            aos.closeArchiveEntry();

        }
    }

    private String entryName(String baseFolderPath, File file) {
        String absoluteBasePath = file.getAbsolutePath();
        String path = absoluteBasePath.substring(baseFolderPath.length());
        return path;
    }

}
