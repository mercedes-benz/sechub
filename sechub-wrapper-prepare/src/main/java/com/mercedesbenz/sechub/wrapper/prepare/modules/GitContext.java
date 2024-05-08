package com.mercedesbenz.sechub.wrapper.prepare.modules;

public class GitContext extends ToolContext {
    private boolean cloneWithoutHistory;

    private GitContext(GitContextBuilder builder) {
        super(builder);
        this.cloneWithoutHistory = builder.cloneWithoutHistory;
    }

    public boolean isCloneWithoutHistory() {
        return cloneWithoutHistory;
    }

    public static class GitContextBuilder extends ToolContextBuilder {
        private boolean cloneWithoutHistory = true;

        public GitContextBuilder setCloneWithoutHistory(boolean cloneWithoutHistory) {
            this.cloneWithoutHistory = cloneWithoutHistory;
            return this;
        }

        @Override
        public GitContext build() {
            return new GitContext(this);
        }
    }
}
