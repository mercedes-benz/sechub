// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

public class AsciidocUtil {

    public static boolean isEmptyAsciidocContent(String content) {
        if (content == null || content.length() == 0) {
            return true;
        }
        boolean justEmpty = true;
        String[] lines = content.split("\n");

        for (String line : lines) {
            boolean ignore = false;

            ignore = ignore || line.startsWith("//");
            ignore = ignore || line.startsWith("----");
            ignore = ignore || line.startsWith("|===");
            ignore = ignore || line.startsWith("[source");
            ignore = ignore || line.startsWith("[option");
            ignore = ignore || line.trim().isEmpty();

            if (!ignore) {
                justEmpty = false;
                break;
            }
        }
        return justEmpty;
    }
}
