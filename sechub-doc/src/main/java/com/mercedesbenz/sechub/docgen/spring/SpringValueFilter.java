// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import com.mercedesbenz.sechub.docgen.spring.SpringValueExtractor.SpringValue;

public interface SpringValueFilter {
    boolean isFiltered(SpringValue data);

    public static class AcceptAllSpringValueFilter implements SpringValueFilter {

        public static final SpringValueFilter.AcceptAllSpringValueFilter INSTANCE = new AcceptAllSpringValueFilter();

        private AcceptAllSpringValueFilter() {
        };

        public boolean isFiltered(SpringValue data) {
            return false;
        }
    }
}