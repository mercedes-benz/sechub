// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.metadata;

public enum SerecoAnnotationType {

    /* a info reported by a product - suitable to be reported back to user */
    USER_INFO(false),

    /* a warning reported by a product - suitable to be reported back to user */
    USER_WARNING(false),

    /* an error reported by a product - suitable to be reported back to user */
    USER_ERROR(false),

    /* an internal product failure - NOT suitable to be reported back to user */
    INTERNAL_ERROR_PRODUCT_FAILED(true),

    /*
     * an internal info message that a product and its result has been successfully
     * imported - NOT suitable to be reported back to user
     *
     * Will be used to handle traffic light (OFF): Only when at least one Sereco
     * annotation of this type is found inside the Sereco report, we assume that we
     * can calculate a traffic light (red/green/yellow). Otherwise Traffic light
     * will be OFF
     */
    INTERNAL_INFO_PRODUCT_SUCCESSFUL_IMPORTED(true),

    ;

    private boolean internal;

    private SerecoAnnotationType(boolean internal) {
        this.internal = internal;
    }

    public boolean isInternal() {
        return internal;
    }
}
