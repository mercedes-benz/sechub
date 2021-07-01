// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * /** Reporting descriptor reference, see <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317638">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
@JsonPropertyOrder({ "ruleId", "level", "message", "locations", "properties" })
public class Result {
    private String ruleId;
    private Message message;
    private Level level;

    private int ruleIndex;

    private List<Location> locations;
    private List<CodeFlow> codeflows;

    private PropertyBag properties = new PropertyBag();

    public Result() {
        this(null, null);
    }

    public Result(String ruleId, Message message) {
        this.ruleId = ruleId;
        this.message = message;

        this.locations = new LinkedList<Location>();
        this.codeflows = new LinkedList<CodeFlow>();
    }

    public void setRuleIndex(int ruleIndex) {
        this.ruleIndex = ruleIndex;
    }

    /**
     * https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317644
     * 
     * @return rule index
     */
    public int getRuleIndex() {
        return ruleIndex;
    }

    public List<CodeFlow> getCodeFlows() {
        return codeflows;
    }

    public boolean addLocation(Location location) {
        if (location == null) {
            return false;
        }
        return this.locations.add(location);
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public PropertyBag getProperties() {
        return properties;
    }

    public void setProperties(PropertyBag properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Result [" + (ruleId != null ? "ruleId=" + ruleId + ", " : "") + (level != null ? "level=" + level + ", " : "")
                + (message != null ? "message=" + message + ", " : "") + (locations != null ? "locations=" + locations + ", " : "")
                + (properties != null ? "properties=" + properties : "") + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeflows, level, locations, message, properties, ruleId, ruleIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Result other = (Result) obj;
        return Objects.equals(codeflows, other.codeflows) && level == other.level && Objects.equals(locations, other.locations)
                && Objects.equals(message, other.message) && Objects.equals(properties, other.properties) && Objects.equals(ruleId, other.ruleId)
                && ruleIndex == other.ruleIndex;
    }

}
