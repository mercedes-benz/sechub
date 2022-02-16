package com.mercedesbenz.sechub.sarif.model;

public class Body {

    private String binary;
    private String text;

    public void setBinary(String binaryAsBase64EncodedString) {
        this.binary = binaryAsBase64EncodedString;
    }

    public String getBinary() {
        return binary;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
