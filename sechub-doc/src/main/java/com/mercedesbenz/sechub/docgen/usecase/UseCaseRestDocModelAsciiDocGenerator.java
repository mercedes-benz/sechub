// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.docgen.AsciidocUtil;
import com.mercedesbenz.sechub.docgen.RestDocResourceModel;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel.UseCaseEntry;
import com.mercedesbenz.sechub.docgen.util.TextFileReader;
import com.mercedesbenz.sechub.docgen.util.TextFileWriter;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseGroup;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc.SpringRestDocOutput;

public class UseCaseRestDocModelAsciiDocGenerator {

    private TextFileReader reader = new TextFileReader();

    // [options="header",cols="1,1,1"]
    // |===
    // |HeadA |HeadB |HeadC
    // //----------------------
    // |Row1A |Row1B |Row1C
    // |Row2A |Row2B |Row2Cs
    // |Row3A |Row3B |Row3C
    // |===
    public String generateAsciidoc(TextFileWriter writer, UseCaseRestDocModel model, boolean technical, UseCaseIdentifier... usecases) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(model);

        Context context = new Context();
        for (UseCaseIdentifier identifier : usecases) {
            context.usecasesToInspect.add(identifier.name());
        }

        int h = 3;
        context.addLine("[[section-ucrestdoc]]");
        context.addLine(headline(h++) + "Rest API documentation");
        context.addLine("");
        context.addLine("ifdef::techdoc[]");
        context.addLine("TIP: The complete documentation about REST API is generated. If you want to change content, please search for ");
        context.addLine("     `@UseCaseRestDoc` references in source code and make necessary changes inside code!");
        context.addLine("");
        context.addLine("endif::techdoc[]");
        List<List<UseCaseRestDocEntry>> found = generateOverview(model, context, h);
        generateUseCaseAndRestDocDetails(context, h, found);
        return context.getAsciiDoc();
    }

    private void generateUseCaseAndRestDocDetails(Context context, int h, List<List<UseCaseRestDocEntry>> found) {
        Set<String> avoidDoubleGeneration = new LinkedHashSet<>();
        for (List<UseCaseRestDocEntry> entries : found) {

            UseCaseRestDocEntry first = entries.iterator().next();
            if (avoidDoubleGeneration.contains(first.identifier)) {
                continue;
            }
            avoidDoubleGeneration.add(first.identifier);
            context.addLine(headline(h) + first.usecaseEntry.getTitle());
            context.addLine(UseCaseAsciiDocFactory.createAnkerUseCaseRestDoc(first.usecaseEntry));

            context.addLine("REST API for usecase " + UseCaseAsciiDocFactory.createLinkToUseCase(first));

            for (UseCaseRestDocEntry entry : entries) {
                boolean multipleVariants = entries.size() > 1;
                generateDetail(context, h, entry, multipleVariants);
            }
        }
    }

    private List<List<UseCaseRestDocEntry>> generateOverview(UseCaseRestDocModel model, Context context, int h) {
        context.addLine(headline(h) + "Overview");
        List<List<UseCaseRestDocEntry>> found = new ArrayList<>();
        for (UseCaseGroup group : UseCaseGroup.values()) {
            SortedSet<UseCaseEntry> entriesForGroup = model.getUseCaseModel().getGroup(group).getUseCases();
            if (entriesForGroup.isEmpty()) {
                continue;
            }
            StringBuilder linksToRestDocs = new StringBuilder();
            createEntriesForGroup(model, context, found, entriesForGroup, linksToRestDocs);
            boolean foundAtLeastOneRestDocForGroup = linksToRestDocs.length() > 0;
            if (!foundAtLeastOneRestDocForGroup) {
                continue;
            }
            context.addLine(headline(h + 1) + group.getTitle());
            context.addLine(group.getDescription());
            context.addLine("");
            context.addLine(linksToRestDocs.toString());
            context.addLine("");
        }
        return found;
    }

    private void createEntriesForGroup(UseCaseRestDocModel model, Context context, List<List<UseCaseRestDocEntry>> found,
            SortedSet<UseCaseEntry> entriesForGroup, StringBuilder linksToRestDocs) {
        for (UseCaseEntry entry : entriesForGroup) {
            if (!context.usecasesToInspect.contains(entry.getIdentifierEnumName())) {
                continue;
            }
            entry.getIdentifierEnumName();
            List<UseCaseRestDocEntry> restDocEntries = model.getRestDocEntries(entry);
            if (restDocEntries == null || restDocEntries.isEmpty()) {
                continue; /* use case has no rest doc so ignore */
            }
            linksToRestDocs
                    .append("- " + UseCaseAsciiDocFactory.createLinkToUseCaseRestDoc(entry, "REST API for " + entry.getId() + "-" + entry.getTitle()) + "\n");
            found.add(restDocEntries);
        }
    }

    private void generateDetail(Context context, int h, UseCaseRestDocEntry entry, boolean multipleVariants) {
        context.addLine("");
        context.addLine(UseCaseAsciiDocFactory.createAnker(entry));
        if (multipleVariants) {
            String line = headline(h + 1);
            if (entry.variantOriginValue.equals(UseCaseRestDoc.DEFAULT_VARIANT)) {
                line += "Standard";
            } else {
                line += entry.variantOriginValue;
            }
            line = line + " variant";
            context.addLine(line);
        }

        /* generate REST doc details */
        Map<SpringRestDocOutput, File> map = mapRestDocOutputFilesInOrderedMap(context, entry);
        appendRestDefinition(context, entry, map);
        appendRestExample(context, entry, map);

    }

    private void appendRestExample(Context context, UseCaseRestDocEntry entry, Map<SpringRestDocOutput, File> map) {
        context.addLine("\n[.big]*Example*");

        for (SpringRestDocOutput restDocOutput : entry.wanted) {
            if (restDocOutput.isExample()) {
                File outputFile = map.get(restDocOutput);
                if (outputFile != null && outputFile.exists()) {
                    addDescription(context, entry, outputFile);
                }
            }
        }
    }

    private void appendRestDefinition(Context context, UseCaseRestDocEntry entry, Map<SpringRestDocOutput, File> map) {
        context.addLine("\n[.big]*Definition*");
        appendGeneralRequestInformation(context, map);

        for (SpringRestDocOutput restDocOutput : entry.wanted) {
            if (restDocOutput.isDefinition()) {
                File outputFile = map.get(restDocOutput);
                if (outputFile != null && outputFile.exists()) {
                    addDescription(context, entry, outputFile);
                }
            }
        }
    }

    private void appendGeneralRequestInformation(Context context, Map<SpringRestDocOutput, File> map) {
        File resourceFile = map.get(SpringRestDocOutput.RESOURCE);
        if (resourceFile != null) {
            String json = reader.loadTextFile(resourceFile);

            RestDocResourceModel model = RestDocResourceModel.fromString(json);
            context.addLine("[options=\"header\",cols=\"1,6\",title=\"General request information\"]");
            context.addLine("|===");
            context.addLine("|                  |Value");
            context.addLine("|Path      |" + model.request.path);
            context.addLine("|Method       |" + model.request.method);
            context.addLine("|Status code  |" + createHttpStatusCodeDescription(model.response.status));
            context.addLine("|===");
        }
    }

    private String createHttpStatusCodeDescription(int statusCode) {
        if (statusCode <= 0) {
            return "";
        }
        HttpStatus status = HttpStatus.valueOf(statusCode);
        return status.toString();
    }

    private Map<SpringRestDocOutput, File> mapRestDocOutputFilesInOrderedMap(Context context, UseCaseRestDocEntry entry) {
        // create a linked hash map - so we can output ordering from annotation
        Map<SpringRestDocOutput, File> map = new LinkedHashMap<>();

        File[] files = entry.copiedRestDocFolder.listFiles();

        /* ordered first: */
        for (SpringRestDocOutput value : entry.wanted) {
            for (File file : files) {
                if (value.isRepresentedBy(file)) {
                    map.put(value, file);
                }
            }
        }

        /* all other after them */
        for (SpringRestDocOutput value : SpringRestDocOutput.values()) {
            if (map.containsKey(value)) {
                // already added by "wanted" parts.
                continue;
            }
            /* not "wanted" - but we add it as well */
            for (File file : files) {
                if (value.isRepresentedBy(file)) {
                    map.put(value, file);
                }
            }
        }
        return map;
    }

    private void addDescription(Context context, UseCaseRestDocEntry entry, File file) {
        context.addLine("");
        String prettyFileName = createPrettyPrintedFileName(file);
        context.addLine("*" + prettyFileName + "*  +");
        if (isGeneratedFileEmpty(file)) {
            context.addLine("_(empty)_");
        } else {
            context.addLine("\ninclude::" + entry.path + "/" + file.getName() + "[]");
        }
    }

    private boolean isGeneratedFileEmpty(File file) {
        boolean justEmpty = false;
        if (SpringRestDocOutput.RESPONSE_BODY.isRepresentedBy(file)) {
            String content = reader.loadTextFile(file, "\n");
            justEmpty = AsciidocUtil.isEmptyAsciidocContent(content);
        }
        return justEmpty;
    }

    private String createPrettyPrintedFileName(File file) {
        String prettyFileName = file.getName();
        prettyFileName = prettyFileName.substring(0, prettyFileName.length() - 5);// remove ".adoc" e.g. curl-request.adoc ->curl-request
        prettyFileName = prettyFileName.replaceAll("-", " "); // e.g.curl-request -> curl request
        char c = prettyFileName.charAt(0);
        prettyFileName = Character.toUpperCase(c) + prettyFileName.substring(1); // E.g. curl request -> Curl request
        return prettyFileName;
    }

    private String headline(int nr) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");// always add a new line - to prevent layout problems
        for (int i = 0; i < nr; i++) {
            sb.append("=");
        }
        return sb.toString() + " ";
    }

    private class Context {

        public Set<String> usecasesToInspect = new LinkedHashSet<>();
        StringBuilder sb = new StringBuilder();

        void addLine(String text) {
            sb.append(text).append('\n');
        }

        public String getAsciiDoc() {
            return sb.toString();
        }
    }

}
