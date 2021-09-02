// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.project;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectAccessLevel {

    FULL("full", 100, "Full access to project, no restrictions"),

    READ_ONLY("read_only", 50, "Users have only read access to existing data, No new jobs possible"),

    NONE("none", 0, "Users have no access at all."),

    ;

    private String id;
    private int level;
    private String description;

    private ProjectAccessLevel(String id, int level, String description) {
        if (id.length() > 10) {
            throw new IllegalArgumentException("We accept only 3x10 in database (UTF-8) so 10  , but was:" + id.length());
        }
        this.id = id;
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Compares this access level to another one and checks if satisfied by given
     * other level
     * 
     * @param found the level to compare with.
     * @return <code>true</code> when other level is lower or equal
     */
    public boolean isEqualOrHigherThan(ProjectAccessLevel found) {
        if (found == null) {
            /* if nothing found we give full access, so always satisfied */
            return true;
        }
        return this.getLevel() >= found.getLevel();
    }

    /**
     * Returns project access level for given identifier
     * 
     * @param id
     * @return project access level or <code>null</code>
     */
    public static ProjectAccessLevel fromId(String id) {
        for (ProjectAccessLevel accessLevel : ProjectAccessLevel.values()) {
            if (accessLevel.id.equals(id)) {
                return accessLevel;
            }
        }
        return null;
    }

}
