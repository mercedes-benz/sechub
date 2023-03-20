package com.mercedesbenz.sechub.systemtest.runtime;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.systemtest.config.SystemTestConfiguration;

public class WrongConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static WrongConfigurationException buildException(String message) {
        return buildException(message, null);
    }

    public static WrongConfigurationException buildException(String message, SystemTestConfiguration config) {
        if (config == null) {
            return new WrongConfigurationException(message);
        }
        String json = JSONConverter.get().toJSON(config, true);

        WrongConfigurationException exception = new WrongConfigurationException(message + "\nConfig as JSON:\n" + json);
        return exception;
    }

    private WrongConfigurationException(String message) {
        super(message);

    }

}
