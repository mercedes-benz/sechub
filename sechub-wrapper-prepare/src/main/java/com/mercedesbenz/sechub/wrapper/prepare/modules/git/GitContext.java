package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;

public class GitContext extends ToolContext {
    private boolean cloneWithoutHistory;

    private String filename;

    private GitContext(GitContextBuilder builder) {
        super(builder);
        this.cloneWithoutHistory = builder.cloneWithoutHistory;
        this.filename = builder.filename;
    }

    public boolean isCloneWithoutHistory() {
        return cloneWithoutHistory;
    }

    public String getFilename() {
        return filename;
    }

    public static class GitContextBuilder extends ToolContextBuilder {
        private boolean cloneWithoutHistory = true;

        private String filename = "GitDownloadDirectory";

        public GitContextBuilder setCloneWithoutHistory(boolean cloneWithoutHistory) {
            this.cloneWithoutHistory = cloneWithoutHistory;
            return this;
        }

        public GitContextBuilder setFilename(String filename) {
            this.filename = filename;
            return this;
        }

        @Override
        public GitContext build() {
            return new GitContext(this);
        }
    }
}
