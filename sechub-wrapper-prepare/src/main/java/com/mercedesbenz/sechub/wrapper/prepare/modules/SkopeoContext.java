package com.mercedesbenz.sechub.wrapper.prepare.modules;

public class SkopeoContext extends ToolContext {

    private final String filename;

    private SkopeoContext(SkopeoContextBuilder builder) {
        super(builder);
        this.filename = builder.filename;
    }

    public String getFilename() {
        return filename;
    }

    public static class SkopeoContextBuilder extends ToolContextBuilder {

        private String filename = "SkopeoDownloadFile.tar";

        @Override
        public SkopeoContext build() {
            return new SkopeoContext(this);
        }

        public SkopeoContextBuilder filename(String filename) {
            if (filename == null || filename.isBlank()) {
                return this;
            }
            if (!filename.endsWith(".tar")) {
                throw new IllegalArgumentException("Filename must end with .tar");
            }
            this.filename = filename;
            return this;
        }
    }
}