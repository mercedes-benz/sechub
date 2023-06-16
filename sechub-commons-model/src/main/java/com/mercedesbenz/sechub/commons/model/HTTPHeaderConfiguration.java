package com.mercedesbenz.sechub.commons.model;

import java.util.List;
import java.util.Optional;

public class HTTPHeaderConfiguration {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_ONLY_FOR_URLS = "onlyForUrls";

    private String name;
    private String value;
    private Optional<List<String>> onlyForUrls = Optional.empty();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Optional<List<String>> getOnlyForUrls() {
        return onlyForUrls;
    }

    public void setOnlyForUrls(Optional<List<String>> onlyForUrls) {
        this.onlyForUrls = onlyForUrls;
    }

}
