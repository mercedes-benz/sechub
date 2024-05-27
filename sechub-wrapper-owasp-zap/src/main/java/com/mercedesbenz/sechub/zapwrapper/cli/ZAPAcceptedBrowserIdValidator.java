// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import com.mercedesbenz.sechub.zapwrapper.config.ZAPAcceptedBrowserId;

public class ZAPAcceptedBrowserIdValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (value == null) {
            throw new ParameterException("Specified browser id was null, only following browser ids are accepted: " + ZAPAcceptedBrowserId.values());
        }
        boolean isValid = false;
        for (ZAPAcceptedBrowserId browserId : ZAPAcceptedBrowserId.values()) {
            if (browserId.getBrowserId().equalsIgnoreCase(value)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new ParameterException("Specified browser id was " + value + ", only follwing browser ids are accepted: " + ZAPAcceptedBrowserId.values());
        }
    }

}