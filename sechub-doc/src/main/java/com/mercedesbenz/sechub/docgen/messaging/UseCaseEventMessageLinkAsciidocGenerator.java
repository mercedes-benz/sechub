// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.messaging;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.docgen.usecase.UseCaseAsciiDocFactory;
import com.mercedesbenz.sechub.docgen.util.DocGenTextFileWriter;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

/**
 * Generates links between usecases and messages
 *
 * @author Albert Tregnaghi
 *
 */
public class UseCaseEventMessageLinkAsciidocGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseEventMessageLinkAsciidocGenerator.class);
    private DocGenTextFileWriter writer;
    private File outputFolder;
    private Map<UseCaseIdentifier, Set<String>> usecaseToMessageIdMap = new TreeMap<>();

    public UseCaseEventMessageLinkAsciidocGenerator(Map<UseCaseIdentifier, Set<String>> usecaseToMessageIdMap, File outputFolder) {
        this.outputFolder = outputFolder;
        this.usecaseToMessageIdMap = usecaseToMessageIdMap;
        this.writer = new DocGenTextFileWriter();
    }

    public void generate() throws IOException {
        LOG.info("start creating link files between usecase and message ids into:" + outputFolder.getAbsolutePath());

        for (UseCaseIdentifier identifier : UseCaseIdentifier.values()) {
            generateLinkUsecaseToMessagesFile(identifier);
        }
        for (MessageID messageId : MessageID.values()) {
            generateLinkMessageToUsecasesFile(messageId);
        }

    }

    private void generateLinkUsecaseToMessagesFile(UseCaseIdentifier identifier) throws IOException {
        AsciidocBuilder ab = generateLinkUsecaseToMessagesContent(identifier);
        String id = UseCaseEventOverviewPlantUmlGenerator.createUsecaseId(identifier);
        File file = new File(outputFolder, "usecase2messages_" + id + ".adoc");

        writer.writeTextToFile(file, ab.getAsciiDoc());
    }

    private AsciidocBuilder generateLinkUsecaseToMessagesContent(UseCaseIdentifier identifier) {
        AsciidocBuilder ab = new AsciidocBuilder();
        Set<String> messageIds = usecaseToMessageIdMap.get(identifier);
        if (messageIds == null || messageIds.isEmpty()) {
            ab.add("// no message ids for usecase " + identifier);
            return ab;
        }
        ab.add("[NOTE]");
        ab.add("====");
        ab.add("Involved messages\n");

        for (String messageId : messageIds) {
            ab.add("- <<" + DomainMessagingFilesGenerator.createMessagingLinkId(messageId) + "," + createTitle(identifier) + ">>");
        }
        ab.add("====");
        return ab;
    }

    private void generateLinkMessageToUsecasesFile(MessageID identifier) throws IOException {
        AsciidocBuilder ab = generateLinkMessageToUsecasesContent(identifier);
        String id = DomainMessagingFilesGenerator.createMessagingLinkId(identifier);
        File file = new File(outputFolder, "message2usecases_" + id + ".adoc");

        writer.writeTextToFile(file, ab.getAsciiDoc());
    }

    private AsciidocBuilder generateLinkMessageToUsecasesContent(MessageID identifier) {
        AsciidocBuilder ab = new AsciidocBuilder();

        Set<UseCaseIdentifier> useCaseIdentifiers = filterRelatedUseCases(identifier);
        if (useCaseIdentifiers == null || useCaseIdentifiers.isEmpty()) {
            ab.add("// no usecases for message id:" + identifier);
            return ab;
        }
        ab.add("[NOTE]");
        ab.add("====");
        ab.add("Use cases related to this message\n");
        for (UseCaseIdentifier useCaseIdentifier : useCaseIdentifiers) {
            String useCaseTitle = createTitle(useCaseIdentifier);

            ab.add("- " + UseCaseAsciiDocFactory.createLinkToUseCase(useCaseIdentifier, useCaseTitle));
        }
        ab.add("====");
        return ab;
    }

    private String createTitle(UseCaseIdentifier useCaseIdentifier) {
        /*
         * we must build use case title here, because we have no access to UseCaseDef
         * objects but only enum
         */
        String useCaseTitle = useCaseIdentifier.name();
        useCaseTitle = useCaseTitle.replaceAll("UC_", "");
        useCaseTitle = useCaseIdentifier.uniqueId() + "-" + useCaseTitle;
        return useCaseTitle;
    }

    private Set<UseCaseIdentifier> filterRelatedUseCases(MessageID identifier) {
        Set<UseCaseIdentifier> uiSet = new TreeSet<>();
        for (UseCaseIdentifier ui : usecaseToMessageIdMap.keySet()) {
            Set<String> messageIds = usecaseToMessageIdMap.get(ui);
            if (messageIds.contains(identifier.name())) {
                uiSet.add(ui);
            }
        }
        return uiSet;
    }

    private class AsciidocBuilder {
        StringBuilder sb = new StringBuilder();

        public void add(String line) {
            sb.append(line);
            sb.append("\n");
        }

        public String getAsciiDoc() {
            return sb.toString();
        }
    }

}
