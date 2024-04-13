// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import java.util.UUID;
import java.util.regex.Pattern;

public class TestTemplateSupport {

    private static final int MAXIMUM_STAR_PLACEHOLDER_AMOUNT = 10000;
    private static final Pattern SECHUB_JOBUUID_PATTERN = Pattern.compile("\\{sechub.jobuuid\\}");
    private static final String START_STAR_PLACEHOLDER = "{*";
    private static final String END_PLACEHOLDER = "}";

    private UUID secHubJobUUID;

    /**
     * Calculates match result for given template and given content. Content and
     * template are transformed: They are trimmed, any whitespace is removed and
     * place holders are used for special content. The result can be checked for
     * deltas.
     *
     * Following place holders are supported:
     *
     * <table border="1">
     * <tr>
     * <th>Placeholder</td>
     * <th>Description</td></th>
     * </tr>
     * <tr>
     * <td>$&#123;sechub.jobuuid&#125;</td>
     * <td>Represents the current SecHub job UUID</td>
     * </tr>
     * <tr>
     * <td>$&#123;*:$amount&#125;</td>
     * <td>The star placeholder represents a given amount of chars. -for example:
     * '$&#123;*:36&#125;' could be used for an unknown UUID (which has 36
     * characters)</td>
     * </tr>
     * </table>
     *
     * @param template is the template string containing placeholders etc.
     * @param content  is the content which shall be compared with the template
     * @return {@link TemplateMatchResult}
     */
    public TemplateMatchResult calculateTemplateMatching(String template, String content) {

        if (template == null) {
            throw new IllegalArgumentException("Template may never be null!");
        }
        TemplateMatchResult data = new TemplateMatchResult();
        data.transformedTemplate = template.trim().replaceAll("\s", "");

        if (content == null) {
            return data;
        }
        data.transformedContent = content.trim().replaceAll("\s", "");

        if (secHubJobUUID != null) {
            data.transformedTemplate = SECHUB_JOBUUID_PATTERN.matcher(data.transformedTemplate).replaceAll(secHubJobUUID.toString());
        }

        int index;
        while ((index = data.transformedTemplate.indexOf(START_STAR_PLACEHOLDER)) != -1) {
            int endIndex = data.transformedTemplate.indexOf(END_PLACEHOLDER, index);

            int nextStartIndex = -1;
            if (data.transformedTemplate.length() > index) {
                nextStartIndex = data.transformedTemplate.indexOf("{", index + 1);
            }
            if (endIndex == -1 || (nextStartIndex != -1 && nextStartIndex < endIndex)) {
                throw new TestTemplateException("A '" + END_PLACEHOLDER + "' is missing after index:" + index);
            }

            String statementWithoutEnd = data.transformedTemplate.substring(index, endIndex);
            int replacements = parseStarPlaceHolderReplacementAndReturnAmount(statementWithoutEnd);
            String replacementString = "x".repeat(replacements);

            data.transformedTemplate = data.transformedTemplate.substring(0, index) + replacementString + data.transformedTemplate.substring(endIndex + 1);

            if (data.transformedContent.length() < index + replacements) {
                return data;
            }
            data.transformedContent = data.transformedContent.substring(0, index) + replacementString + data.transformedContent.substring(index + replacements);
        }
        return data;
    }

    public class TemplateMatchResult {

        private String transformedContent;
        private String transformedTemplate;

        /**
         *
         * @return content which has been transformed with information from origin
         *         template so it is equal to the transformed template (when matching)
         */
        public String getTransformedContent() {
            return transformedContent;
        }

        /**
         *
         * @return template with place holders replaced with data
         */
        public String getTransformedTemplate() {
            return transformedTemplate;
        }

        public boolean isMatching() {
            return transformedTemplate.equals(transformedContent);
        }

    }

    private int parseStarPlaceHolderReplacementAndReturnAmount(String statement) {
        // {:$amount
        if (statement.length() < 4) {
            failStarPlaceHolder(statement, null);
        }
        String inspect = statement.substring(3);
        int charsToReplace = 0;
        try {
            charsToReplace = Integer.parseInt(inspect);
            if (charsToReplace > MAXIMUM_STAR_PLACEHOLDER_AMOUNT) {
                failStarPlaceHolder(statement, "Given amount is bigger than maximum of " + MAXIMUM_STAR_PLACEHOLDER_AMOUNT + " chars!");
            }
        } catch (NumberFormatException e) {
            failStarPlaceHolder(statement, "'" + inspect + "' cannot be used as amount, because it is not an integer.");
        }
        return charsToReplace;
    }

    private void failStarPlaceHolder(String statementStart, String additional) {
        throw new TestTemplateException("Star placeholder syntax is {*:$amountOfCharsToIgnore} - given was: " + statementStart + END_PLACEHOLDER + "."
                + (additional != null ? " " + additional : ""));

    }

    public void setSecHubJobUUID(UUID secHubJobUUID) {
        this.secHubJobUUID = secHubJobUUID;

    }

}
