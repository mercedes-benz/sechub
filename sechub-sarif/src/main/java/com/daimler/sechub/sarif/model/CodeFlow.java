package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;

public class CodeFlow {

    Message message;
    
    List<ThreadFlow> threadFlows;
    
    public CodeFlow() {
        threadFlows=new LinkedList<>();
    }
    
    public List<ThreadFlow> getThreadFlows() {
        return threadFlows;
    }
}


