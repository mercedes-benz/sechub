package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import java.io.File;
import java.io.FileFilter;

public class AutoCleanupAdditionalGitFilesFilter implements FileFilter {

    public static AutoCleanupAdditionalGitFilesFilter INSTANCE = new AutoCleanupAdditionalGitFilesFilter();

    private AutoCleanupAdditionalGitFilesFilter() {
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
            return false;
        }
        return name.equals(".gitattributes") || name.equals(".gitignore");
    }

}
