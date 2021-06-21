// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Reporting description object, see see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317836">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonPropertyOrder({ "id", "name", "shortDescription", "fullDescription", "help", "properties" })
public abstract class ReportingDescriptor {

    private String id;

    // see
    // https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128041
    private ReportingConfiguration defaultConfiguration;

    private String name;
    private Message shortDescription;
    private Message fullDescription;
    private Message help;
    private PropertyBag properties;

    private List<ReportingDescriptorRelationship> relationships;

    public ReportingDescriptor() {
        this.relationships = new LinkedList<>();
    }

    public ReportingConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public void setDefaultConfiguration(ReportingConfiguration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
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

    public PropertyBag getProperties() {
        return properties;
    }

    public void setProperties(PropertyBag properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Rule [id=" + id + ", name=" + name + ", shortDescription=" + shortDescription + ", fullDescription=" + fullDescription + ", help=" + help
                + ", properties=" + properties + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullDescription, help, id, name, properties, relationships, shortDescription);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportingDescriptor other = (ReportingDescriptor) obj;
        return Objects.equals(fullDescription, other.fullDescription) && Objects.equals(help, other.help) && Objects.equals(id, other.id)
                && Objects.equals(name, other.name) && Objects.equals(properties, other.properties) && Objects.equals(relationships, other.relationships)
                && Objects.equals(shortDescription, other.shortDescription);
    }

}