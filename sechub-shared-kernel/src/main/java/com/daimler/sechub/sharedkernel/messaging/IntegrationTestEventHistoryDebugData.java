package com.daimler.sechub.sharedkernel.messaging;

import java.util.Map;
import java.util.TreeMap;

public class IntegrationTestEventHistoryDebugData {

    private String senderThread;
    private Map<String,String> messageData= new TreeMap<>();

    public void setSenderThread(String name) {
        this.senderThread=name;
    }
    
    public void setMessageData(Map<String, String> messageData) {
        this.messageData = messageData;
    }
    
    public Map<String, String> getMessageData() {
        return messageData;
    }
    
    public String getSenderThread() {
        return senderThread;
    }
}
