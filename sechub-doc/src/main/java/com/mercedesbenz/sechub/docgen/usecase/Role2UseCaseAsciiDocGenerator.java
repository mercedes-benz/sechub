// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry.UseCaseEntryStep;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;

public class Role2UseCaseAsciiDocGenerator {

    // [options="header",cols="1,1,1"]
    // |===
    // |HeadA |HeadB |HeadC
    // //----------------------
    // |Row1A |Row1B |Row1C
    // |Row2A |Row2B |Row2Cs
    // |Row3A |Row3B |Row3C
    // |===
    public String generateAsciidoc(UseCaseModel model) {

        Context context = new Context();
        int headlineLevel = 5;
        context.addLine("");
        context.addLine("[[section-sechub-role-lists]]");
        context.addLine(headline(headlineLevel++) + "Role 2 Use cases");
        context.addLine("");

        for (String role : RoleConstants.getAllRoles()) {
            generateRoleTable(role, model, context, headlineLevel);
        }

        return context.getAsciiDoc();
    }

    private void generateRoleTable(String role, UseCaseModel model, Context context, int headlineLevel) {
        context.addLine(createAnker(role));
        context.addLine(headline(headlineLevel) + "Usecase list for role " + role);
        for (UseCaseEntry entry : model.getUseCases()) {
            for (UseCaseEntryStep step : entry.getSteps()) {
                if (!step.getRolesAllowed().contains(role)) {
                    continue;
                }
                context.addLine("- <<" + UseCaseAsciiDocFactory.createLinkId(entry) + "," + entry.getId() + "-" + entry.getTitle() + ">>\n");
            }
        }
    }

    private static String createAnker(String role) {
        return "[[" + createSectionId(role) + "]]";
    }

    static String createSectionId(String role) {
        return "section-role2usecase-list-" + role;
    }

    private String headline(int nr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nr; i++) {
            sb.append("=");
        }
        return sb.toString() + " ";
    }

    private class Context {

        StringBuilder sb = new StringBuilder();

        void addLine(String text) {
            sb.append(text).append('\n');
        }

        public String getAsciiDoc() {
            return sb.toString();
        }
    }

}
