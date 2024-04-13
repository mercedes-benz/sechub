// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mercedesbenz.sechub.systemtest.runtime.SystemTestRuntimeException;
import com.mercedesbenz.sechub.systemtest.runtime.variable.EnvironmentProvider;

public class SystemTestTemplateEngine {

    private enum ParseState {

        DOLLAR_DETECTED,

        VAR_OPEND,

        VAR_CLOSED,

        NONE,

    }

    public boolean hasEnvironmentVariables(String string) {
        return hasVariablesOfType(string, TemplateVariableType.ENV);
    }

    public boolean hasSecretEnvironmentVariables(String string) {
        return hasVariablesOfType(string, TemplateVariableType.SECRET_ENV);
    }

    public boolean hasUserVariables(String string) {
        return hasVariablesOfType(string, TemplateVariableType.USER_VARIABLES);
    }

    public boolean hasRuntimeVariables(String string) {
        return hasVariablesOfType(string, TemplateVariableType.RUNTIME_VARIABLES);
    }

    public boolean hasVariablesOfType(String string, TemplateVariableType prefix) {
        return string.contains("${" + prefix.getFullPrefix());
    }

    public String replaceUserVariablesWithValues(String string, Map<String, String> variables) {
        return replaceVariables(string, new GenericMapVariableResolver(variables, TemplateVariableType.USER_VARIABLES));
    }

    public String replaceRuntimeVariablesWithValues(String string, Map<String, String> runtimeVariables) {
        return replaceVariables(string, new GenericMapVariableResolver(runtimeVariables, TemplateVariableType.RUNTIME_VARIABLES));
    }

    public String replaceEnvironmentVariablesWithValues(String string, EnvironmentProvider environmentProvider) {
        return replaceVariables(string, new GenericEnvironentVariableResolver(environmentProvider, TemplateVariableType.ENV));
    }

    public String replaceSecretEnvironmentVariablesWithValues(String string, EnvironmentProvider environmentProvider) {
        return replaceVariables(string, new GenericEnvironentVariableResolver(environmentProvider, TemplateVariableType.SECRET_ENV));
    }

    private String replaceVariables(String string, VariableValueResolver resolver) {
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        List<TemplateVariableBlock> templateVariables = parseVariableBlocks(string);
        /*
         * we reverse order to last one on top - means we can change content without
         * affecting the indexes of the other template variables!
         */
        Collections.reverse(templateVariables);

        for (TemplateVariableBlock templateVariable : templateVariables) {
            String fullVariableName = templateVariable.getName();
            TemplateVariableType variableType = resolver.getType();

            String variablePrefix = variableType.getFullPrefix();

            if (fullVariableName.startsWith(variablePrefix)) {
                String variableName = fullVariableName.substring(variablePrefix.length());
                String variableValue = resolver.resolveValueFor(variableName);
                if (variableValue == null) {
                    String undefinedVariableMessage = "'" + fullVariableName + "' is not defined!\n\n" + "Allowed variables for type " + variableType + " are:";
                    List<String> proposals = resolver.createProposals();

                    for (String proposal : proposals) {
                        undefinedVariableMessage += "\n- " + proposal;
                    }
                    throw new SystemTestRuntimeException(undefinedVariableMessage);
                }
                sb.replace(templateVariable.getStartIndex(), templateVariable.getEndIndex(), variableValue);
            }
        }

        return sb.toString();
    }

    /**
     * Parses given string and extracts template variable blocks. Whitespaces are
     * kept inside complete part of variable block, but will be removed in name.
     *
     * @param content content to parse
     * @return a ordered list of template variables, never <code>null</code>. The
     *         last parsed variable will be at the end of the list.
     */
    public List<TemplateVariableBlock> parseVariableBlocks(String content) {
        List<TemplateVariableBlock> result = new ArrayList<>();

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

    private void parseVariables(String content, List<TemplateVariableBlock> result) {
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

                    TemplateVariableBlock variable = new TemplateVariableBlock();
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
                if (context.state == ParseState.DOLLAR_DETECTED) {
                    /* means we hgave not "${" but only $other */
                    context.state = ParseState.NONE;
                } else if (context.state == ParseState.VAR_OPEND) {
                    if (!Character.isWhitespace(c)) {
                        context.addToVariableName(c);
                    }
                } else {
                    /* just ignore */
                }
                break;
            }

        }
    }

}