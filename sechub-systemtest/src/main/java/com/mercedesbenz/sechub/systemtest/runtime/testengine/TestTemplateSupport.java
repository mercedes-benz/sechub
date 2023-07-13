package com.mercedesbenz.sechub.systemtest.runtime.testengine;

import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTemplateSupport {

    private static final Logger LOG = LoggerFactory.getLogger(TestTemplateSupport.class);

    private static final int MAXIMUM_STAR_PLACEHOLDER_AMOUNT = 10000;
    private static final Pattern SECHUB_JOBUUID_PATTERN = Pattern.compile("\\{sechub.jobuuid\\}");
    private static final String START_STAR_PLACEHOLDER = "{*";
    private static final String END_PLACEHOLDER = "}";

    private UUID secHubJobUUID;

    /**
     * Checks if the given template matches to given content. Content and template
     * are always trimmed, so leading white spaces are removed automatically.
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
     * @param template
     * @param contentToCheckForMatch
     * @return
     */
    public boolean isTemplateMatching(String template, String contentToCheckForMatch) {
        if (template == null) {
            throw new IllegalArgumentException("Template may never be null!");
        }
        if (contentToCheckForMatch == null) {
            return false;
        }

        CompareData data = calculateCompareData(template, contentToCheckForMatch);

        LOG.trace("changedContent=\n{}", data.getChangedContent());
        LOG.trace("changedTemplate=\n{}", data.getChangedTemplate());

        return data.changedContent.equals(data.changedTemplate);
    }

    public CompareData calculateCompareData(String template, String contentToCheckForMatch) {
        CompareData data = new CompareData();
        data.changedTemplate = template.trim();
        data.changedContent = contentToCheckForMatch.trim();

        if (secHubJobUUID != null) {
            data.changedTemplate = SECHUB_JOBUUID_PATTERN.matcher(data.changedTemplate).replaceAll(secHubJobUUID.toString());
        }
        int index;
        while ((index = data.changedTemplate.indexOf(START_STAR_PLACEHOLDER)) != -1) {
            int endIndex = data.changedTemplate.indexOf(END_PLACEHOLDER, index);
            if (endIndex == -1) {
                throw new TestTemplateException("A " + END_PLACEHOLDER + " is missing! after index:" + index);
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

    class CompareData {

        private String changedContent;
        private String changedTemplate;

        String getChangedContent() {
            return changedContent;
        }

        String getChangedTemplate() {
            return changedTemplate;
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
