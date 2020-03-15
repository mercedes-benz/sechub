package com.daimler.sechub.sharedkernel.messaging;

import java.util.ArrayList;
import java.util.List;

public class IntegrationTestEventHistoryInspection {

    @Override
    public String toString() {
        return "IntegrationTestEventHistoryInspection [eventId=" + eventId + ", synchron=" + synchron + ", senderClassName=" + senderClassName
                + ", receiverClassNames=" + receiverClassNames + "]";
    }

    private List<String> receiverClassNames = new ArrayList<>();
    private String senderClassName;
    private boolean synchron;
    private String eventId;

    public boolean isSynchron() {
        return synchron;
    }

    public String getEventId() {
        return eventId;
    }

    public List<String> getReceiverClassNames() {
        return receiverClassNames;
    }

    public String getSenderClassName() {
        return senderClassName;
    }

    public void setSynchronousSender(String className, MessageID messageId) {
        this.synchron = true;
        initializeSenderAndEvent(className, messageId);
    }

    public void setAsynchronousSender(String className, MessageID messageId) {
        this.synchron = false;
        initializeSenderAndEvent(className, messageId);
    }

    private void initializeSenderAndEvent(String className, MessageID messageId) {
        this.senderClassName = className;
        this.eventId = messageId.getId();
    }
}
