package com.mercedesbenz.sechub.systemtest.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.systemtest.runtime.EnvironmentProvider;

public class SystemTestTemplateEngine {

    private static final String ENV_PREFIX = "env.";
    private static final String USER_VARIABLES_PREFIX = "variables.";

    private enum ParseState {

        DOLLAR_DETECTED,

        VAR_OPEND,

        VAR_CLOSED,

        NONE,

    }

    public boolean hasEnvironmentVariables(String string) {
        return string.contains("${" + ENV_PREFIX);
    }

    public boolean hasUserVariables(String string) {
        return string.contains("${" + USER_VARIABLES_PREFIX);
    }

    public String replaceUserVariablesWithValues(String string, Map<String, String> variables) {
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        List<TemplateVariable> templateVariables = parseVariables(string);
        /*
         * we reverse order to last one on top - means we can change content without
         * affecting the indexes of the other template variables!
         */
        Collections.reverse(templateVariables);

        for (TemplateVariable templateVariable : templateVariables) {
            String variableName = templateVariable.getName();
            if (variableName.startsWith(USER_VARIABLES_PREFIX)) {
                String key = variableName.substring(USER_VARIABLES_PREFIX.length());
                String value = variables.get(key);
                if (value == null) {
                    value = "";
                }
                sb.replace(templateVariable.getStartIndex(), templateVariable.getEndIndex(), value);
            }
        }

        return sb.toString();
    }

    public String replaceEnvironmentVariablesWithValues(String string, EnvironmentProvider environmentProvider) {
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        List<TemplateVariable> templateVariables = parseVariables(string);
        /*
         * we reverse order to last one on top - means we can change content without
         * affecting the indexes of the other template variables!
         */
        Collections.reverse(templateVariables);

        for (TemplateVariable templateVariable : templateVariables) {
            String variableName = templateVariable.getName();
            if (variableName.startsWith(ENV_PREFIX)) {
                String envName = variableName.substring(ENV_PREFIX.length());
                String envValue = environmentProvider.getEnv(envName);
                if (envValue == null) {
                    envValue = "";
                }
                sb.replace(templateVariable.getStartIndex(), templateVariable.getEndIndex(), envValue);
            }
        }

        return sb.toString();
    }

    /**
     * Parses given string and extracts variable names. Whitespaces are kept inside
     * complete name of variable, but will be removed in name.
     *
     * @param content content to parse
     * @return a ordered list of template variables, never <code>null</code>. The
     *         last parsed variable will be at the end of the list.
     */
    public List<TemplateVariable> parseVariables(String content) {
        List<TemplateVariable> result = new ArrayList<>();

        parseVariables(content, result);

        return result;
    }

    private class ParseContext {
        private ParseState state;
        private StringBuilder fullSb;
        private StringBuilder nameSb;
        private int startIndex;
        private int endIndex;

        private ParseContext() {
            reset();
        }

        public void addToVariableName(char c) {
            nameSb.append(c);
        }

        public void reset() {
            fullSb = new StringBuilder();
            nameSb = new StringBuilder();

            startIndex = -1;
            endIndex = -1;

            state = ParseState.NONE;
        }

        public void addFull(char c) {
            fullSb.append(c);
        }
    }

    private void parseVariables(String content, List<TemplateVariable> result) {
        char[] charArray = content.toCharArray();

        ParseContext context = new ParseContext();

        for (int index = 0; index < charArray.length; index++) {
            char c = charArray[index];

            context.addFull(c);

            switch (c) {

            case '$':
                if (context.state == ParseState.VAR_OPEND) {
                    context.addToVariableName(c);
                    // when inside a variable definition we just add as content
                } else {
                    context.reset();
                    context.addFull(c);
                    context.state = ParseState.DOLLAR_DETECTED;
                    context.startIndex = index;
                }
                break;
            case '{':
                if (context.state == ParseState.DOLLAR_DETECTED) {
                    context.state = ParseState.VAR_OPEND;
                } else {
                    context.addToVariableName(c);
                }
                break;

            case '}':
                if (context.state == ParseState.VAR_OPEND) {
                    context.endIndex = index + 1;

                    TemplateVariable variable = new TemplateVariable();
                    variable.name = context.nameSb.toString();
                    variable.complete = context.fullSb.toString();
                    variable.startIndex = context.startIndex;
                    variable.endIndex = context.endIndex;

                    result.add(variable);

                    context.reset();

                } else {
                    context.state = ParseState.DOLLAR_DETECTED;
                    context.startIndex = index;
                }
                break;

            default:
                if (context.state == ParseState.VAR_OPEND) {
                    if (!Character.isWhitespace(c)) {
                        context.addToVariableName(c);
                    }
                }
                break;
            }

        }
    }

}