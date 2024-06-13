package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperContext;

public abstract class AbstractPrepareWrapperModule implements PrepareWrapperModule {

    @Override
    public final void prepare(PrepareWrapperContext context) throws IOException {
        prepareImpl(context);

        String userMessageForPreparationDone = getUserMessageForPreparationDone();
        if (userMessageForPreparationDone != null) {
            context.getUserMessages().add(new SecHubMessage(SecHubMessageType.INFO, userMessageForPreparationDone));
        }

    }

    protected abstract void prepareImpl(PrepareWrapperContext context) throws IOException;

    protected void ensureDirectoryExists(Path path) {
        if (Files.exists(path)) {
            return;
        }
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Error while creating download directory: " + path, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
