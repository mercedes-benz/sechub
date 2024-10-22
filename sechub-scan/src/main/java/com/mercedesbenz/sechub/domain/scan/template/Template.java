// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.template;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents a template
 *
 * @author Albert Tregnaghi
 *
 */
@Entity
@Table(name = Template.TABLE_NAME)
public class Template {

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SQL ......................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String TABLE_NAME = "SCAN_TEMPLATE";

    public static final String COLUMN_TEMPLATE_ID = "TEMPLATE_ID";
    public static final String COLUMN_DEFINITION = "TEMPLATE_DEFINITION";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ JPQL .....................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String CLASS_NAME = "Template";

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_DEFINITION = "definition";

    public static final String QUERY_All_TEMPLATE_IDS = "select t.id from #{#entityName} t";

    @Id
    @Column(name = COLUMN_TEMPLATE_ID, updatable = false, nullable = false)
    String id;

    @Column(name = COLUMN_DEFINITION)
    String definition;

    @Version
    @Column(name = "VERSION")
    @JsonIgnore
    Integer version;

    Template() {
        // jpa only
    }

    public Template(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

}
