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
     * Calculates compare data between given template and given content. Content and
     * template are always trimmed, so leading white spaces are removed
     * automatically. At both sides changes are done with place holders. The result
     * contains the "normalized" content which can be compared.
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
     * @return compare data which contains changed template and content data. If the
     *         template is matching both parts are equal
     */
    public TemplateMatchResult calculateTemplateMatching(String template, String content) {

        if (template == null) {
            throw new IllegalArgumentException("Template may never be null!");
        }
        TemplateMatchResult data = new TemplateMatchResult();
        data.changedTemplate = template.trim();

        if (content == null) {
            return data;
        }
        data.changedContent = content.trim();

        if (secHubJobUUID != null) {
            data.changedTemplate = SECHUB_JOBUUID_PATTERN.matcher(data.changedTemplate).replaceAll(secHubJobUUID.toString());
        }

        int index;
        while ((index = data.changedTemplate.indexOf(START_STAR_PLACEHOLDER)) != -1) {
            int endIndex = data.changedTemplate.indexOf(END_PLACEHOLDER, index);

            int nextStartIndex = -1;
            if (data.changedTemplate.length() > index) {
                nextStartIndex = data.changedTemplate.indexOf("{", index + 1);
            }
            if (endIndex == -1 || (nextStartIndex != -1 && nextStartIndex < endIndex)) {
                throw new TestTemplateException("A '" + END_PLACEHOLDER + "' is missing after index:" + index);
            }

            String statementWithoutEnd = data.changedTemplate.substring(index, endIndex);
            int replacements = parseStarPlaceHolderReplacementAndReturnAmount(statementWithoutEnd);
            String replacementString = "x".repeat(replacements);

            data.changedTemplate = data.changedTemplate.substring(0, index) + replacementString + data.changedTemplate.substring(endIndex + 1);

            if (data.changedContent.length() < index + replacements) {
                return data;
            }
            data.changedContent = data.changedContent.substring(0, index) + replacementString + data.changedContent.substring(index + replacements);
        }
        return data;
    }

    public class TemplateMatchResult {

        private String changedContent;
        private String changedTemplate;

        public String getChangedContent() {
            return changedContent;
        }

        public String getChangedTemplate() {
            return changedTemplate;
        }

        public boolean isMatching() {
            return changedTemplate.equals(changedContent);
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
