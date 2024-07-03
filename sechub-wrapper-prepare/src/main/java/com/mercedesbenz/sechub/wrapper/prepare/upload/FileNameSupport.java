// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FileNameSupport {
    public List<Path> getTarFilesFromDirectory(Path directory) {
        assertDirectory(directory);

        List<Path> result = new ArrayList<>();
        File[] files = directory.toFile().listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".tar")) {
                result.add(file.toPath());
            }
        }
        return result;
    }

    public List<Path> getRepositoriesFromDirectory(Path directory) {
        assertDirectory(directory);

        List<Path> repositories = new ArrayList<>();
        File[] files = directory.toFile().listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                repositories.add(f.toPath());
            }
        }
        return repositories;
    }

    private void assertDirectory(Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Directory parameter may not be null.");
        }

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Directory parameter '" + directory + "' is not a directory");
        }
    }
}
