package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class FileNameSupport {
    public List<Path> getTarFilesFromDirectory(Path path) {
        if (Files.isDirectory(path)) {
            List<Path> result = new ArrayList<>();
            File[] files = path.toFile().listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".tar")) {
                    result.add(file.toPath());
                }
            }
            return result;
        } else {
            throw new IllegalArgumentException("Parameter " + " is not a directory");
        }
    }

    public List<Path> getRepositoriesFromDirectory(Path path) {
        List<Path> repositories = new ArrayList<>();

        if (path == null) {
            throw new IllegalArgumentException("File may not be null!");
        }

        File[] files = path.toFile().listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                repositories.add(f.toPath());
            }
        }
        return repositories;
    }
}
