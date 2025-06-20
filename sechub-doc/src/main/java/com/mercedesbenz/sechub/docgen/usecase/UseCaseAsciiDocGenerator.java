// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.docgen.GeneratorConstants;
import com.mercedesbenz.sechub.docgen.messaging.UseCaseEventOverviewPlantUmlGenerator;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseDefGroup;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry.UseCaseEntryStep;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseModelType;
import com.mercedesbenz.sechub.pds.usecase.PDSUseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.Step;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;

public class UseCaseAsciiDocGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseAsciiDocGenerator.class);
    private static final Pattern PATTERN_MATCH_UNDERSCORE = Pattern.compile("_");
    // [options="header",cols="1,1,1"]
    // |===
    // |HeadA |HeadB |HeadC
    // //----------------------
    // |Row1A |Row1B |Row1C
    // |Row2A |Row2B |Row2Cs
    // |Row3A |Row3B |Row3C
    // |===
    private boolean connectUsecaseWithMessage;
    private boolean showEventDiagrams;

    public String generateAsciidoc(UseCaseModel model, File diagramsGenFolder) {
        return generateAsciidoc(model, diagramsGenFolder, true, true);
    }

    public String generateAsciidoc(UseCaseModel model, File diagramsGenFolder, boolean connectUsecaseWithMessage, boolean showEventDiagrams) {
        this.connectUsecaseWithMessage = connectUsecaseWithMessage;
        this.showEventDiagrams = showEventDiagrams;

        Context context = new Context();
        context.diagramsGenFolder = diagramsGenFolder;
        int headlineLevel = 3;
        context.addLine("");
        context.addLine(":usecasedoc:");
        context.addLine("");
        context.addLine("[[section-usecases]]");
        context.addLine(headline(headlineLevel++) + "Use cases");
        context.addLine("");
        context.addLine("ifdef::techdoc[]");
        context.addLine("TIP: The complete documentation about use cases is generated. If you want to change content, please search for ");
        context.addLine("     `@UseCaseDefinition` references in source code and make necessary changes inside code!");
        context.addLine("endif::techdoc[]");
        context.addLine("");
        generateOverview(model, context, headlineLevel);
        generateDetails(model, context, headlineLevel);

        return context.getAsciiDoc();
    }

    private void generateDetails(UseCaseModel model, Context context, int headlineLevel) {
        for (UseCaseEntry entry : model.getUseCases()) {
            generateUseCase(context, headlineLevel, entry);
        }
    }

    private void generateOverview(UseCaseModel model, Context context, int headlineLevel) {
        context.addLine(headline(headlineLevel) + "Overview about usecase groups");
        UseCaseModelType type = model.getType();
        switch (type) {
        case SECHUB:
            for (UseCaseGroup useCaseGroup : UseCaseGroup.values()) {
                generateGroupUseCaseLinks(context, headlineLevel, model.getGroup(useCaseGroup));
            }
            break;
        case PDS:
            for (PDSUseCaseGroup useCaseGroup : PDSUseCaseGroup.values()) {
                generateGroupUseCaseLinks(context, headlineLevel, model.getGroup(useCaseGroup));
            }
            break;
        default:
            throw new IllegalArgumentException("Unsupported type:" + type);
        }
    }

    private void generateGroupUseCaseLinks(Context context, int h, UseCaseDefGroup group) {
        SortedSet<UseCaseEntry> entries = group.getUseCases();
        if (entries.isEmpty()) {
            return;
        }
        context.addLine(headline(h + 1) + group.title);
        context.addLine(group.description);
        context.addLine("");
        for (UseCaseEntry entry : entries) {
            context.addLine("- <<" + UseCaseAsciiDocFactory.createLinkId(entry) + "," + entry.getId() + "-" + entry.getTitle() + ">>\n");
        }
        context.addLine("");
    }

    private void generateUseCase(Context context, int headlineLevel, UseCaseEntry entry) {
        context.addLine(UseCaseAsciiDocFactory.createAnker(entry));
        context.addLine(headline(headlineLevel) + entry.getId() + "-" + entry.getTitle());
        context.addLine(entry.getDescription());
        context.addLine("");

        context.addLine("ifdef::techdoc[]");
        context.addLine("*Technical information*");
        context.addLine("");
        context.addLine("You will find relevant code parts by searching for references of `@" + entry.getAnnotationName() + "`");
        context.addLine("");
        context.addLine("endif::techdoc[]");

        if (showEventDiagrams) {
            generateEventTraceOverviewIfPreGenerated(context, entry);
        }
        if (connectUsecaseWithMessage) {
            context.addLine("include::usecase2messages_" + entry.getIdentifierEnumName().toLowerCase() + ".adoc[]");
        }

        context.addLine("*Steps*");
        generateUseCaseStepsTable(context, entry);
        generateLinkToRestAPIDoc(context, entry);
    }

    /**
     * Generates include of plantuml diagrams which are generated by
     * {@link UseCaseEventOverviewPlantUmlGenerator}
     *
     * @param context
     * @param entry
     */
    private void generateEventTraceOverviewIfPreGenerated(Context context, UseCaseEntry entry) {
        // we generate only asciidoc parts here, when pre-generation did create parts
        // At least currently, not every usecase has got such an overview
        // so we need to check if pre-generated files are existing

        String subFolderPath = UseCaseEventOverviewPlantUmlGenerator.createPlantumlFolderSubPathByUsecase(entry.getIdentifierEnumName());
        File usecaseOverViewFolder = new File(context.diagramsGenFolder, subFolderPath);
        if (!usecaseOverViewFolder.exists()) {
            if (GeneratorConstants.DEBUG) {
                LOG.warn("Event-Trace MISSING:{} - No event-trace overview folder found at:{}", entry.getIdentifierEnumName(),
                        usecaseOverViewFolder.getAbsolutePath());
            }
            return;
        }

        if (GeneratorConstants.DEBUG) {
            LOG.info("Event-Trace FOUND  :{} - Event-trace overview folder found  at:{}", entry.getIdentifierEnumName(),
                    usecaseOverViewFolder.getAbsolutePath());
        }

        File[] overviewFiles = usecaseOverViewFolder.listFiles();
        if (overviewFiles == null) {
            if (GeneratorConstants.DEBUG) {
                LOG.warn("Event-Trace MISSING(2):{} - No event-trace overview folder found at:{}", entry.getIdentifierEnumName(),
                        usecaseOverViewFolder.getAbsolutePath());
            }
            return;
        }

        Arrays.sort(overviewFiles);

        for (File overViewFile : overviewFiles) {
            String variant = UseCaseEventOverviewPlantUmlGenerator.filenameVariantconverter.getVariantFromFilename(overViewFile.getName());
            String targetFilePath = UseCaseEventOverviewPlantUmlGenerator.createPlantumlFileSubPathByUsecase(entry.getIdentifierEnumName(), variant);
            String variantDescription = createDescriptionForVariant(variant);
            context.addLine("*Event overview" + variantDescription + "*");
            context.addLine("");
            context.addLine("plantuml::diagrams/gen/" + targetFilePath + "[format=svg, alt=\"Overview of events happening at usecase "
                    + entry.getIdentifierEnumName() + variantDescription + "]\n");
            context.addLine("");
        }
    }

    protected String createDescriptionForVariant(String variant) {
        String description = "";
        if (variant != null && !variant.isEmpty()) {
            description = " - variant: " + PATTERN_MATCH_UNDERSCORE.matcher(variant).replaceAll(" ").trim();
        }
        return description;
    }

    private void generateLinkToRestAPIDoc(Context context, UseCaseEntry entry) {
        if (entry.getRestDocEntries().isEmpty()) {
            return;
        }
        UseCaseRestDocEntry docEntry = entry.getRestDocEntries().iterator().next();
        context.addLine("ifdef::techdoc[]");
        context.addLine(UseCaseAsciiDocFactory.createLinkToUseCaseRestDoc(docEntry) + "\n");
        context.addLine("endif::techdoc[]");
    }

    private void generateUseCaseStepsTable(Context context, UseCaseEntry entry) {
        context.addLine("[options=\"header\",cols=\"5,20,20,5,50\"]");
        context.addLine("|===");
        context.addLine("|Nr |Title |Role(s)|Next|Description");
        context.addLine("//----------------------");
        generateUseCaseStepsTableEntries(context, entry);
        context.addLine("|===");
        context.addLine("");
    }

    private void generateUseCaseStepsTableEntries(Context context, UseCaseEntry entry) {
        List<UseCaseEntryStep> steps = entry.getSteps();
        Collections.sort(steps);
        Collections.reverse(steps);

        StringBuilder rows = new StringBuilder();
        int lastStep = 0;
        for (UseCaseEntryStep step : steps) {
            StringBuilder sb = new StringBuilder();

            sb.append("|");
            sb.append(step.getNumber()); /* nr */
            sb.append("|");
            sb.append(step.getTitle()); /* title */
            sb.append("|");
            sb.append(createRolesString(step)); /* roles */
            sb.append("|");
            sb.append(createNextStep(lastStep, step)); /* next */
            sb.append("|");
            sb.append(step.getDescription()); /* description */
            sb.append("\n");
            sb.append("\n");
            sb.append("ifdef::techdoc[]\n");
            sb.append("__This step is defined at " + step.getLocation() + "__");
            sb.append("\n");
            sb.append("endif::techdoc[]");
            sb.append("\n");
            String row = sb.toString();
            rows.insert(0, row);

            lastStep = step.getNumber();
        }
        context.sb.append(rows);
    }

    private String createNextStep(int lastStepNumber, UseCaseEntryStep step) {
        StringBuilder visibleNext = new StringBuilder();
        int[] next = step.getNext();
        if (next.length > 0) {
            int counter = 0;
            for (int n : next) {
                counter++;
                if (n == Step.NO_NEXT_STEP) {
                    // means no further steps! (as described in @Step annotation )
                    continue;
                }
                visibleNext.append(n);
                if (counter < next.length) {
                    visibleNext.append(", ");
                }
            }
        } else {
            if (lastStepNumber > 0) {
                visibleNext.append(lastStepNumber);
            }
        }
        return visibleNext.toString();
    }

    private String createRolesString(UseCaseEntryStep step) {
        StringBuilder sbx = new StringBuilder();
        Set<String> rolesAllowed = step.getRolesAllowed();
        if (!rolesAllowed.isEmpty()) {
            for (Iterator<String> ri = rolesAllowed.iterator(); ri.hasNext();) {
                String role = ri.next();
                sbx.append("<<" + Role2UseCaseAsciiDocGenerator.createSectionId(role) + "," + role + ">>");
                if (ri.hasNext()) {
                    sbx.append(", ");
                }
            }
        }
        return sbx.toString();
    }

    private String headline(int nr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nr; i++) {
            sb.append("=");
        }
        return sb.toString() + " ";
    }

    private class Context {

        public File diagramsGenFolder;
        StringBuilder sb = new StringBuilder();

        void addLine(String text) {
            sb.append(text).append('\n');
        }

        public String getAsciiDoc() {
            return sb.toString();
        }
    }

}
