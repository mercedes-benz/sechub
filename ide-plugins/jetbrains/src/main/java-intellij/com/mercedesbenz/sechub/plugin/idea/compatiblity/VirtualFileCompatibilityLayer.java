// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.compatiblity;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;

/**
 * IntelliJ 2020.2.3 has got some nice utils inside VirtualFile - able to converter virtual file directly to nio. This class
 * is a compatibility layer to provide the functionality to be backward compatible to intelliJ 2020.1. This is necessary
 * to support Android Studio 4.1 which still relies on 2020.1
 */
public class VirtualFileCompatibilityLayer {

    /**
     * Provides similar functionality like virtualFile.toNioPath() in 2020.2.3 - backward compatible to 2020.1
     * @param file
     * @return path
     */
    public static @NotNull Path toNioPath(VirtualFile file) {
        if (file==null){
            throw new UnsupportedOperationException("Failed to map , file null not supported!");
        }
        String path = file.getPath();
        if (path==null){
            throw new UnsupportedOperationException("Failed to map , path null not supported!");
        }
        //this would be the way 2020.2.3 would do...

        // VirtualFileSystem fileSystem = file.getFileSystem();
        // if (fileSystem==null){
        //     throw new UnsupportedOperationException("Failed to get filesystem , fileSystem null not supported!");
        // }
        // Path path = fileSystem.getNioPath(file);

        File javaIOFile = new File(path);
        return javaIOFile.toPath();
        // if (path == null) {
        //    throw new UnsupportedOperationException("Failed to map " + file + " (filesystem " + fileSystem + ") into nio Path");
        //}
        //return path;
    }
}
