// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SpringUtilFactory {

    /**
     * Creates a default jackson object mapper
     *
     * @return a default jackson object mapper
     */
    public static ObjectMapper createDefaultObjectMapper() {
        return new ObjectMapper();
    }
}
