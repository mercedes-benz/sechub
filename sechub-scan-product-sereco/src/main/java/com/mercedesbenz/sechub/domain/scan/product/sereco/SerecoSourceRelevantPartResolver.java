// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class SerecoSourceRelevantPartResolver {

    private static final Pattern P = Pattern.compile("\\s");

    /**
     * Converts given source to relevant part
     *
     * @param source
     * @return relevant part, never <code>null</code>
     */
    public String toRelevantPart(String source) {
        if (source == null) {
            return "";
        }
        String result = P.matcher(source).replaceAll("");
        return result.toLowerCase();
    }
}
