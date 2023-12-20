// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportException;

public class ZipFileExtractor {

    public boolean fileExists(String filename) {
        return Files.exists(Path.of(filename));
    }

    /**
     * Unzips the report received from the artifactory @see <a
     * href="https://mkyong.com/java/how-to-decompress-files-from-a-zip-file/"/a>
     *
     * @param source source zip file
     * @param target target folder
     * @throws IOException
     */
    public void unzipFile(Path source, Path target) throws XrayWrapperReportException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                boolean isDirectory = false;
                if (zipEntry.getName().endsWith(File.separator)) {
                    isDirectory = true;
                }

                Path newPath = zipSlipProtect(zipEntry, target);

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }

                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();
        } catch (IOException e) {
            throw new XrayWrapperReportException("Could not extract zip file.", XrayWrapperExitCode.IO_ERROR, e);
        }
    }

    /*
     * Please see: <a href=https://security.snyk.io/research/zip-slip-vulnerability
     * /a>
     */
    private Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws XrayWrapperReportException {
        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new XrayWrapperReportException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
}
