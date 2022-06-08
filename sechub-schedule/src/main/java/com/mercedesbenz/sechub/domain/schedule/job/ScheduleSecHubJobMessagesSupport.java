package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;

public class ScheduleSecHubJobMessagesSupport {

    public List<SecHubMessage> fetchMessagesOrNull(ScheduleSecHubJob job) {
        if (job == null) {
            return null;
        }
        String jsonMessages = job.getJsonMessages();
        if (jsonMessages == null) {
            return null;
        }
        return JSONConverter.get().fromJSONtoListOf(SecHubMessage.class, jsonMessages);
    }

    public void addMessages(ScheduleSecHubJob job, List<SecHubMessage> messagesToAdd) {
        if (messagesToAdd == null || messagesToAdd.isEmpty()) {
            return;
        }
        List<SecHubMessage> foundMessages = fetchMessagesOrNull(job);
        List<SecHubMessage> messages = null;
        if (foundMessages != null) {
            messages = foundMessages;
        } else {
            messages = new ArrayList<>(messagesToAdd.size() + 1);
        }

        messages.addAll(messagesToAdd);
        job.setJsonMessages(JSONConverter.get().toJSON(messages));

    }
}
