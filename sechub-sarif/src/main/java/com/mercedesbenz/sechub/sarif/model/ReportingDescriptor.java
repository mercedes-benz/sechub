// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sarif.model;

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
@JsonPropertyOrder({ "id", "guid", "name", "shortDescription", "fullDescription", "helpUri", "help" })
public abstract class ReportingDescriptor extends SarifObject {

    private String id;
    private String guid;
    // see
    // https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128041

    private String name;

    private Message shortDescription;
    private Message fullDescription;
    private String helpUri;
    private Message help;

    private ReportingConfiguration defaultConfiguration;
    private List<ReportingDescriptorRelationship> relationships;

    public ReportingDescriptor() {
        this.relationships = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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

    public String getHelpUri() {
        return helpUri;
    }

    public void setHelpUri(String helpUri) {
        this.helpUri = helpUri;
    }

    public Message getHelp() {
        return help;
    }

    public void setHelp(Message help) {
        this.help = help;
    }

    public ReportingConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public void setDefaultConfiguration(ReportingConfiguration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    public List<ReportingDescriptorRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<ReportingDescriptorRelationship> relationships) {
        this.relationships = relationships;
    }

    @Override
    public String toString() {
        return "ReportingDescriptor [id=" + id + ", guid=" + guid + ", name=" + name + ", shortDescription=" + shortDescription + ", fullDescription="
                + fullDescription + ", helpUri=" + helpUri + ", help=" + help + ", defaultConfiguration=" + defaultConfiguration + ", relationships="
                + relationships + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(defaultConfiguration, fullDescription, guid, help, helpUri, id, name, relationships, shortDescription);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportingDescriptor other = (ReportingDescriptor) obj;
        return Objects.equals(defaultConfiguration, other.defaultConfiguration) && Objects.equals(fullDescription, other.fullDescription)
                && Objects.equals(guid, other.guid) && Objects.equals(help, other.help) && Objects.equals(helpUri, other.helpUri)
                && Objects.equals(id, other.id) && Objects.equals(name, other.name) && Objects.equals(relationships, other.relationships)
                && Objects.equals(shortDescription, other.shortDescription);
    }

}