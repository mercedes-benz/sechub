// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.pds;

/**
 * These keys are transfered to script as ENV variables.
 * @author Albert Tregnaghi
 *
 */
public class RuntimeEnvironmentKey extends AbstractPDSKey<RuntimeEnvironmentKey>{

    public RuntimeEnvironmentKey(String id, String description) {
        super(id, description);
    }

}
