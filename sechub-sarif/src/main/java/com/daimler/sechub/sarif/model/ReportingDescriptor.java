package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "name", "shortDescription", "fullDescription", "help", "properties" })
public class ReportingDescriptor {

    private String id;
    private String name;
    private Message shortDescription;
    private Message fullDescription;
    private Message help;
    private Properties properties;
    
    
    private List<ReportingDescriptorRelationship> relationships;

    public ReportingDescriptor() {
        this(null,null,null,null,null,null);
    }

    public ReportingDescriptor(String id, String name, Message shortDescription, Message fullDescription, Message help,
            Properties properties) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.help = help;
        this.properties = properties;
        
        this.relationships=new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ReportingDescriptorRelationship> getRelationships() {
        return relationships;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Message getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(Message shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Message getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(Message fullDescription) {
        this.fullDescription = fullDescription;
    }

    public Message getHelp() {
        return help;
    }

    public void setHelp(Message help) {
        this.help = help;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Rule [id=" + id + ", name=" + name + ", shortDescription=" + shortDescription + ", fullDescription="
                + fullDescription + ", help=" + help + ", properties=" + properties + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReportingDescriptor)) {
            return false;
        }
        ReportingDescriptor other = (ReportingDescriptor) obj;
        return Objects.equals(fullDescription, other.fullDescription) && Objects.equals(help, other.help)
                && Objects.equals(id, other.id) && Objects.equals(name, other.name)
                && Objects.equals(properties, other.properties)
                && Objects.equals(shortDescription, other.shortDescription);
    }

}