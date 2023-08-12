// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.template;

import java.util.Objects;

class TemplateVariableBlock {

    String name;
    String complete;

    int startIndex;
    int endIndex;

    public String getComplete() {
        return complete;
    }

    public String getName() {
        return name;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(complete, endIndex, name, startIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TemplateVariableBlock other = (TemplateVariableBlock) obj;
        return Objects.equals(complete, other.complete) && endIndex == other.endIndex && Objects.equals(name, other.name) && startIndex == other.startIndex;
    }

}
