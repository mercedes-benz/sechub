package com.mercedesbenz.sechub.xraywrapper.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ReportExtractor {

    /**
     * Check if file exists
     *
     * @param filename
     */
    public static void fileExists(String filename) {
        if (!(Files.exists(Path.of(filename)))) {
            // todo: Error handling
            System.out.println("Could not find reports archive " + filename);
            System.exit(0);
        }
    }

    /**
     * Unzips the report received from the artifactory
     *
     * @param source
     * @param target
     * @throws IOException
     */
    public static void unzipReports(Path source, Path target) throws IOException {
        // https://mkyong.com/java/how-to-decompress-files-from-a-zip-file/
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
        }
    }

    /**
     * Protect from zip slip attack
     *
     * @param zipEntry
     * @param targetDir
     * @return
     * @throws IOException
     */
    private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
}
