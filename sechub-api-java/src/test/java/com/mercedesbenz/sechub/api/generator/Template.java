// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api.generator;

class Template {
    StringBuilder sb = new StringBuilder();

    String getCode() {
        return sb.toString();
    }

    void addLine(String string) {
        sb.append(string);
        sb.append("\n");
    }
}