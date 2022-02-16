// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds;

/**
 * These keys are used at PDS to execute PDS jobs
 *
 * @author Albert Tregnaghi
 *
 */
public class ExecutionPDSKey extends AbstractPDSKey<ExecutionPDSKey> {

    private boolean availableInsideScript;

    public ExecutionPDSKey(String id, String description) {
        super(id, description);
    }

    ExecutionPDSKey markAsAvailableInsideScript() {
        availableInsideScript = true;
        return this;
    }

    public boolean isAvailableInsideScript() {
        return availableInsideScript;
    }

}
