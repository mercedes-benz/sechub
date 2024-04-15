// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal.autoclean;

public enum ActionState {

    /**
     * I can determine a state (i am sure). Means no retry necessary,
     */
    DONE_CAN_MAKE_STATEMENT,

    /**
     * Cannot determine state - please retry ...
     */
    PLEASE_GO_FURTHER,
}