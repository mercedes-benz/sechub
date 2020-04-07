package com.daimler.sechub.sharedkernel.messaging;

import java.util.ArrayList;
import java.util.List;

public class EventInspection {

    private List<EventInspection> children = new ArrayList<>();

    public List<EventInspection> getChildren() {
        return children;
    }
}
