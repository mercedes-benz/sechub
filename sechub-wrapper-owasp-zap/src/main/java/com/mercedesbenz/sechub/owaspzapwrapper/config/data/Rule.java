// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

public class Rule {

    private String id;
    private String ref;
    private String name;
    private String type;
    private String link;

    /**
     *
     * @return id or <code>null</code> if not set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return ref or <code>null</code> if not set.
     */
    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     *
     * @return Name or <code>null</code> if not set.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return type or <code>null</code> if not set.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return link or <code>null</code> if not set.
     */
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
