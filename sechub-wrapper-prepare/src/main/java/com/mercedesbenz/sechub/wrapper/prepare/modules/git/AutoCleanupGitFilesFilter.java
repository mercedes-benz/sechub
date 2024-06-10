package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.io.File;
import java.io.FileFilter;

public class AutoCleanupGitFilesFilter implements FileFilter {

    public static AutoCleanupGitFilesFilter INSTANCE = new AutoCleanupGitFilesFilter();

    private AutoCleanupGitFilesFilter() {
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
        return name.equals(".gitattributes") || name.equals(".gitignore");
    }

}
