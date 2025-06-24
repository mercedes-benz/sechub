// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ContainerPathUtils {

    public Path resolveScriptpath(Class<?> clazz, String subPath) {
        Path path = resolveScriptByFile(subPath);
        if (path != null) {
            return path;
        }
        return resolvePathByClass(clazz, subPath);
    }

    public File resolvePackageBuildTmpFolder(Class<?> clazz) throws IOException {
        String workdir = Paths.get("").toAbsolutePath().toString();

        if (workdir.endsWith("sechub")) {
            File folder = new File("sechub-developertools/build/tmp");
            if (!folder.exists()) {
                Files.createDirectories(folder.toPath());
            }
            return folder;

        } else if (workdir.endsWith("sechub-developertools")) {
            File folder = new File("build/tmp");
            if (!folder.exists()) {
                Files.createDirectories(folder.toPath());
            }
            return folder;
        } else {

            // Fallback using class location
            String classLocation = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
            Path classPath = Paths.get(classLocation).getParent();

            return classPath.resolve("../../../build/tmp").normalize().toFile();
        }
    }

    private Path resolveScriptByFile(String subPath) {
        File file = new File("./scripts/container/" + subPath);
        if (file.exists()) {
            return file.toPath();
        }

        /*
         * Some IDEs like JetBrains use root folder as current directory, so we do here
         * a fallback
         */
        file = new File("./sechub-developer-tools/scripts/container/" + subPath);
        if (file.exists()) {
            return file.toPath();
        }

        return null;
    }

    private Path resolvePathByClass(Class<?> clazz, String subPath) {
        // Resolve the script path relative to the class location
        // It should not matter where the TestContainer is executed from
        String classLocation = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        Path classPath = Paths.get(classLocation).getParent();

        return classPath.resolve(Paths.get("../../../scripts/container", subPath)).normalize();
    }
}
