// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecHubMessagesList implements JSONable<SecHubMessagesList> {

    private static final SecHubMessagesList IMPORTER = new SecHubMessagesList();

    private List<SecHubMessage> secHubMessages = new ArrayList<>();

    private String type = "sechubMessagesList";

    public SecHubMessagesList(Collection<SecHubMessage> messages) {
        if (messages == null) {
            return;
        }
        this.secHubMessages.addAll(messages);
    }

    public SecHubMessagesList() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<SecHubMessage> getSecHubMessages() {
        return secHubMessages;
    }

    @Override
    public Class<SecHubMessagesList> getJSONTargetClass() {
        return SecHubMessagesList.class;
    }

    public static final SecHubMessagesList fromJSONString(String json) {
        return IMPORTER.fromJSON(json);
    }

}
