package com.mercedesbenz.sechub.docgen;

import com.mercedesbenz.sechub.systemtest.config.DefaultFallback;

public class SystemTestDocGenerator {

    public String generateDefaultFallbackTable() {
        /* @formatter:off */
        String data = "[options=\"header\"]\n"
                + "|===\n"
                + "|Scope   |Default   \n"
                + "//-------------\n";

        for (DefaultFallback fallback: DefaultFallback.values()) {
            data = data+ "|"+fallback.getScope()+" | `"+ fallback.getValue()+"`   \n";
        }

        data= data
                + "|===\n"
                + "";
        /* @formatter:on */
        return data;

    }
}
