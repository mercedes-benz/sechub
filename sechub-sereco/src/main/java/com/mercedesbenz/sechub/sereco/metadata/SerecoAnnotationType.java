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
     * an internal info message that a product's result has been successfully
     * imported - NOT suitable to be reported back to user
     */
    INTERNAL_INFO_PRODUCT_SUCCESSFUL_IMPORTED(true),
    
    /* an internal product failure - NOT suitable to be reported directly back to user */
    INTERNAL_PRODUCT_CANCELED(true),
    
    ;

    private boolean internal;

    private SerecoAnnotationType(boolean internal) {
        this.internal = internal;
    }

    public boolean isInternal() {
        return internal;
    }
}
