// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

public class WrongConfigurationException extends SystemTestRuntimeException {

    private static final long serialVersionUID = 1L;
    private SystemTestRuntimeContext context;

    private static final String buildMessage(String message, SystemTestRuntimeContext context) {
        String fullmessage = message;
        if (context != null) {
            SystemTestConfiguration originConfig = context.getOriginConfiguration();
            if (originConfig != null) {
                String json = JSONConverter.get().toJSON(originConfig, true);
                fullmessage = fullmessage + "\n" + "==========================\n" + "Origin system test config:\n" + "==========================\n" + "\n"
                        + json;
            }

            SystemTestConfiguration runtimeConfig = context.getConfiguration();
            if (runtimeConfig != null) {
                String json = JSONConverter.get().toJSON(runtimeConfig, true);
                fullmessage = fullmessage + "\n" + "======================================\n" + "Prepared system test config (runtime):\n"
                        + "======================================\n" + "\n" + json;

            }
        }
        return fullmessage;
    }

    public String createDetails() {
        return buildMessage(getLocalizedMessage(), context);
    }

    public WrongConfigurationException(String message, SystemTestRuntimeContext context) {
        this(message, context, null);
    }

    public WrongConfigurationException(String message, SystemTestRuntimeContext context, Exception cause) {
        super(message, cause);
        this.context = context;
    }

}
