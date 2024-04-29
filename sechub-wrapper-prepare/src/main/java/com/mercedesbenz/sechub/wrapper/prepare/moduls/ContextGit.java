package com.mercedesbenz.sechub.wrapper.prepare.moduls;

public class ContextGit extends ContextTool {
    private boolean cloneWithoutHistory;

    private ContextGit(GitContextBuilder builder) {
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
        public ContextGit build() {
            return new ContextGit(this);
        }
    }
}
