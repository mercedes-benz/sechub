// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.integrationtest.api.AssertJobScheduler.TestExecutionState;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestSecHubJobStatus;
import com.mercedesbenz.sechub.integrationtest.internal.SecHubJobAutoDumper;

public class AssertSecHubJobStatus {

    private IntegrationTestSecHubJobStatus status;

    private SecHubJobAutoDumper autoDumper = new SecHubJobAutoDumper();

    public AssertSecHubJobStatus(UUID sechubJobUUID, TestProject projectId) {
        String json = TestAPI.as(TestAPI.SUPER_ADMIN).getJobStatus(projectId, sechubJobUUID);
        initWithJson(json);
    }

    public AssertSecHubJobStatus(String json) {
        initWithJson(json);
    }

    private void initWithJson(String json) {
        status = IntegrationTestSecHubJobStatus.fromJson(json);
    }

    public AssertSecHubJobStatus isInState(TestExecutionState state) {
        return isInState(state.name());
    }
    public AssertSecHubJobStatus isInState(String state) {
        autoDumper.execute(() -> assertEquals(state, status.state));
        return this;
    }

    public AssertSecHubJobStatus hasJobUUID() {
        autoDumper.execute(() -> assertNotNull(status.jobUUID));
        return this;
    }

    public UUID getJobUUID() {
        return status.jobUUID;
    }

    public AssertSecHubJobStatus hasNoMessagesDefined() {
        autoDumper.execute(() -> assertNull(status.messages));
        return this;
    }

    public AssertSecHubJobStatus hasMessages(int amount) {
        List<SecHubMessage> foundMessages = getNullSafeMessages();
        autoDumper.execute(() -> assertEquals("Amount of messages differs", amount, foundMessages.size()));
        return this;
    }

    public AssertSecHubJobStatus enablePDSAutoDumpOnErrorsForSecHubJob(UUID sechubJobUUID) {
        this.autoDumper.enablePDSAutoDumpOnErrorsForSecHubJob();
        this.autoDumper.setSecHubJobUUID(sechubJobUUID);
        return this;
    }

    public AssertSecHubJobStatus hasMessage(SecHubMessageType type, String text) {
        List<SecHubMessage> foundMessages = getNullSafeMessages();
        for (SecHubMessage foundMessage : foundMessages) {
            if (Objects.equals(foundMessage.getType(), type)) {
                if (Objects.equals(foundMessage.getText(), text)) {
                    return this;
                }
            }
        }
        autoDumper.execute(
                () -> fail("The status has no message of type:" + type + ", with text:" + text + "\njson was:\n" + JSONConverter.get().toJSON(status)));
        return null;
    }

    private List<SecHubMessage> getNullSafeMessages() {
        if (status.messages == null) {
            return Collections.emptyList();
        }
        return status.messages;
    }
}
