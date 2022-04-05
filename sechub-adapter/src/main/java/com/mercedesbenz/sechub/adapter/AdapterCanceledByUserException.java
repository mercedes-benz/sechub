// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

public class AdapterCanceledByUserException extends AdapterException {

    private static final long serialVersionUID = 8698367126373773465L;

    public AdapterCanceledByUserException(AdapterLogId id) {
        super(id, "was canceled by user");
    }

}
