package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class TarFileSupport {
    public String getTarFileFromFolder(String folder) {
        // check if download folder contains a .tar archive
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
                    throw new RuntimeException("Error while checking download of docker image");
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while checking download of docker image", e);
            }
        } else {
            throw new IllegalArgumentException("Folder is not a directory");
        }
    }
}
