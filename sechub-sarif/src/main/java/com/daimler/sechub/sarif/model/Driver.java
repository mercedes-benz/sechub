package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "version", "informationUri", "rules" })
public class Driver {
    private String name;
    private String version;
    private String informationUri;

    private List<Rule> rules;

    public Driver() {
        this.rules = new LinkedList<Rule>();
    }

    public Driver(String name, String version, String informationUri, List<Rule> rules) {
        this.name = name;
        this.version = version;
        this.informationUri = informationUri;
        this.rules = rules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInformationUri() {
        return informationUri;
    }

    public void setInformationUri(String informationUri) {
        this.informationUri = informationUri;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "Driver [name=" + name + ", version=" + version + ", informationUri=" + informationUri + ", rules="
                + rules + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Driver)) {
            return false;
        }
        Driver other = (Driver) obj;
        return Objects.equals(informationUri, other.informationUri) && Objects.equals(name, other.name)
                && Objects.equals(rules, other.rules) && Objects.equals(version, other.version);
    }
}
