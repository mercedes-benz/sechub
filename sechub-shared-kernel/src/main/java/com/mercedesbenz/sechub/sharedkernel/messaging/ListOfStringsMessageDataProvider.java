// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.messaging;

import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;

public class ListOfStringsMessageDataProvider implements MessageDataProvider<List<String>> {

    @Override
    public List<String> get(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }
        try {
            return JSONConverter.get().fromJSONtoListOf(String.class, data);
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }
    }

    @Override
    public String getString(List<String> data) {
        if (data == null) {
            return null;
        }
        try {
            return JSONConverter.get().toJSON(data);
        } catch (JSONConverterException e) {
            throw new SecHubRuntimeException("Cannot convert", e);
        }
    }

}
