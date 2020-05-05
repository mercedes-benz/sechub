package com.daimler.sechub.sharedkernel.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IntegrationTestEventHistoryInspection {

    private List<String> receiverClassNames = new ArrayList<>();
    private String senderClassName;
    private boolean synchron;
    private String eventId;
    private IntegrationTestEventHistoryDebugData debug = new IntegrationTestEventHistoryDebugData();
    
    public IntegrationTestEventHistoryDebugData getDebug() {
        return debug;
    }
    
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
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId, receiverClassNames, senderClassName, synchron);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntegrationTestEventHistoryInspection other = (IntegrationTestEventHistoryInspection) obj;
        return Objects.equals(eventId, other.eventId) && 
                receiverClassNames.size()==other.receiverClassNames.size() &&
                receiverClassNames.containsAll(other.receiverClassNames)
                && other.receiverClassNames.containsAll(receiverClassNames)
                && Objects.equals(senderClassName, other.senderClassName) && synchron == other.synchron;
    }

    @Override
    public String toString() {
        return "IntegrationTestEventHistoryInspection [eventId=" + eventId + ", synchron=" + synchron + ", senderClassName=" + senderClassName
                + ", receiverClassNames=" + receiverClassNames + "]";
    }

    private void initializeSenderAndEvent(String className, MessageID messageId) {
        this.senderClassName = className;
        this.eventId = messageId.getId();
    }
}
