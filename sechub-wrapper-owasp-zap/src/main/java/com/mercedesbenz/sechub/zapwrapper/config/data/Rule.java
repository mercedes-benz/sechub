// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config.data;

public class Rule {

    private String id;
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

    /**
     *
     * @return Name or <code>null</code> if not set.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return type or <code>null</code> if not set.
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return link or <code>null</code> if not set.
     */
    public String getLink() {
        return link;
    }

}