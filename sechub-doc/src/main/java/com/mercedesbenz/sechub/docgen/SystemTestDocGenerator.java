package com.mercedesbenz.sechub.docgen;

import com.mercedesbenz.sechub.docgen.adopt.AdoptedSystemTestDefaultFallbacks;

public class SystemTestDocGenerator {

    public String generateDefaultFallbackTable() {
        /* @formatter:off */
        String data = "[options=\"header\"]\n"
                + "|===\n"
                + "|Scope   |Default   \n"
                + "//-------------\n";

        for (AdoptedSystemTestDefaultFallbacks fallback: AdoptedSystemTestDefaultFallbacks.values()) {
            data = data+ "|"+fallback.getScope()+" | `"+ fallback.getValue()+"`   \n";
        }

        data= data
                + "|===\n"
                + "";
        /* @formatter:on */
        return data;

    }
}
