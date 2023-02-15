// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.analytic;

public class CodeAnalyticDataPerLanguage {

    private String language;
    private long lines;
    private long files;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getLines() {
        return lines;
    }

    public void setLines(long lines) {
        this.lines = lines;
    }

    public long getFiles() {
        return files;
    }

    public void setFiles(long files) {
        this.files = files;
    }
}
