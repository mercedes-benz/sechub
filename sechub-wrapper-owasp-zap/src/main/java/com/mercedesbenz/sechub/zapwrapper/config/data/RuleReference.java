// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.config.data;

public class RuleReference {

    private String reference;
    private String info;

    public RuleReference() {
    }

    public RuleReference(String reference, String info) {
        this.reference = reference;
        this.info = info;
    }

    /**
     *
     * @return reference or <code>null</code> if not set.
     */
    public String getReference() {
        return reference;
    }

    /**
     *
     * @return info or <code>null</code> if not set.
     */
    public String getInfo() {
        return info;
    }

}
