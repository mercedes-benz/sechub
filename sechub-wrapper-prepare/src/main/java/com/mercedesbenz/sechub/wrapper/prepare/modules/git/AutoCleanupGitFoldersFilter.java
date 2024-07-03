// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.io.File;
import java.io.FileFilter;

public class AutoCleanupGitFoldersFilter implements FileFilter {

    public static AutoCleanupGitFoldersFilter INSTANCE = new AutoCleanupGitFoldersFilter();

    private AutoCleanupGitFoldersFilter() {
    }

    @Override
    public boolean accept(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName();
        if (name == null) {
            return false;
        }

        if (file.isDirectory()) {
            return name.equals(".git");
        }
        return false;
    }

}
