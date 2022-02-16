// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MockEmailEntry {
    public String from;
    public String to;
    public String subject;
    public String text;
    public String cc;
    public String bcc;

    @Override
    public String toString() {
        return "MockEmailEntry: [subject=" + subject + ", from=" + from + ", to=" + to + ", cco=" + cc + ", bcc=" + bcc + "]";
    }

    public String fullToString() {
        return "MockEmail '" + subject + "'\nfrom=" + from + "\nto=" + to + "\ncc=" + cc + "\nbcc=" + bcc + "\n\n" + text + "]\n";
    }

}