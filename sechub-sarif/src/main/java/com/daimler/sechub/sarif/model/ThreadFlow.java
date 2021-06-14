package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;

public class ThreadFlow {

    private List<ThreadFlowLocation> locations;
    
    public ThreadFlow() {
        locations = new LinkedList<>();
    }
    
    public List<ThreadFlowLocation> getLocations() {
        return locations;
    }
}
