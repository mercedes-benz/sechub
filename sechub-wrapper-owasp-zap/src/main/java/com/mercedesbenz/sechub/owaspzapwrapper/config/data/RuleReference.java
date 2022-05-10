// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.config.data;

public class RuleReference {

    private String ref;
    private String info;

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
     * @return info or <code>null</code> if not set.
     */
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}
