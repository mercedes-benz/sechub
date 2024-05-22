package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class FileNameSupport {
    public String getTarFileNameFromDirectory(String folder) {
        // TODO: 22.05.24 laura we will use this method in skopeo modul for download
        // check
        Path path = Path.of(folder);
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                List<String> result = walk.filter(p -> !Files.isDirectory(p)) // not a directory
                        .map(p -> p.toString().toLowerCase()) // convert path to string
                        .filter(f -> f.endsWith(".tar")) // check end with
                        .toList(); // collect all matched to a List
                if (result.size() == 1) {
                    return result.get(0);
                } else {
                    throw new RuntimeException("Error while try to find .tar file.");
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while try to find .tar file.", e);
            }
        } else {
            throw new IllegalArgumentException("Parameter " + folder + " is not a directory");
        }
    }

    public String getSubfolderFileNameFromDirectory(String folder) {
        Set<String> files = listFilesUsingJavaIO(new File(folder).getAbsolutePath());
        if (files.size() == 1) {
            return files.iterator().next();
        } else if (files.isEmpty()) {
            throw new IllegalArgumentException("Download directory is empty: " + folder);
        } else {
            throw new IllegalArgumentException("Download directory contains more than one subfolder: " + folder);
        }
    }

    private Set<String> listFilesUsingJavaIO(String dir) {
        /* @formatter:off */
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles())).
                filter(File::isDirectory).
                map(File::getName).
                collect(Collectors.toSet());
        /* @formatter:on */
    }

}
