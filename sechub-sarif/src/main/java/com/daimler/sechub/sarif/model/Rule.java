package com.daimler.sechub.sarif.model;

public class Rule extends ReportingDescriptor {

    public Rule() {
        super();
    }

    public Rule(String id, String name, Message shortDescription, Message fullDescription, Message help, Properties properties) {
        super(id, name, shortDescription, fullDescription, help, properties);
    }
    
}
