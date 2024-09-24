// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubMessage implements Comparable<SecHubMessage> {

    private SecHubMessageType type;

    private String text;

    public SecHubMessage() {
        /* just for Jackson JSON serialization */
    }

    public SecHubMessage(SecHubMessageType type, String text) {
        this.type = type;
        this.text = text;
    }

    public void setType(SecHubMessageType type) {
        this.type = type;
    }

    public SecHubMessageType getType() {
        return type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecHubMessage other = (SecHubMessage) obj;
        return Objects.equals(text, other.text) && type == other.type;
    }

    @Override
    public int compareTo(SecHubMessage other) {
        if (other == null) {
            return 1;
        }
        SecHubMessageType otherType = other.type;
        if (otherType == type) {
            return compareOnMessageIfTypeIsSame(other);
        }
        if (otherType == null) {
            return 1;
        }
        if (type == null) {
            return -1;
        }
        int result = type.compareTo(otherType);
        if (result != 0) {
            return result;
        }
        return compareOnMessageIfTypeIsSame(other);
    }

    private int compareOnMessageIfTypeIsSame(SecHubMessage otherNotNull) {
        if (otherNotNull.text == this.text) {
            return 0;
        }
        if (text == null) {
            return -1;
        }
        if (otherNotNull.text == null) {
            return 1;
        }
        return text.compareTo(otherNotNull.text);
    }

    @Override
    public String toString() {
        return "SecHubMessage [" + (type != null ? "type=" + type + ", " : "") + (text != null ? "text=" + text : "") + "]";
    }

}
