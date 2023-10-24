// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileAttributesUtil {

    public static FileAttribute<Set<PosixFilePermission>> createFileAttributes(PosixFilePermission... posixPermissions) {

        Set<PosixFilePermission> filePermissions = new LinkedHashSet<>();
        filePermissions.addAll(Arrays.asList(posixPermissions));
        FileAttribute<Set<PosixFilePermission>> attributes = PosixFilePermissions.asFileAttribute(filePermissions);

        return attributes;
    }

}
