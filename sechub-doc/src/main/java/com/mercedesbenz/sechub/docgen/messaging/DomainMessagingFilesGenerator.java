// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.messaging;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import com.mercedesbenz.sechub.docgen.messaging.DomainMessagingModel.Domain;
import com.mercedesbenz.sechub.docgen.messaging.DomainMessagingModel.DomainPart;
import com.mercedesbenz.sechub.docgen.util.DocGenTextFileWriter;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;

public class DomainMessagingFilesGenerator {

    private DocGenTextFileWriter writer;
    private DomainMessagingModelPlantUMLGenerator domainMessagingModelPlantUMLGenerator;

    public DomainMessagingFilesGenerator(DocGenTextFileWriter writer) {
        this.writer = writer;
        this.domainMessagingModelPlantUMLGenerator = new DomainMessagingModelPlantUMLGenerator();
    }

    public void generateMessagingFiles(File messagingFile, File diagramsGenFolder, DomainMessagingModel model) throws IOException {
        String overviewName = "gen_domain_messaging_overview.plantuml";
        DomainMessagingModel reducedModelForOverview = createReducedClone(model);
        generateMessagingPlantumlFile(diagramsGenFolder, reducedModelForOverview, "__Overview__ of domain **messaging** ", overviewName, MessageID.values());

        for (MessageID messageId : MessageID.values()) {
            String messagePlantUmlFileName = createGeneratedPlantUmlFileName(messageId);
            generateMessagingPlantumlFile(diagramsGenFolder, model, "__Communication details __ of **message " + messageId.getId() + "**",
                    messagePlantUmlFileName, new MessageID[] { messageId });
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[[section-gen-messaging-overview]]\n");
        sb.append("===== Overview\n");
        sb.append("====== Diagram\n");
        sb.append("plantuml::diagrams/gen/" + overviewName + "[format=svg, alt=\"Sequence diagram of messaging overview\"]\n");
        sb.append("\n");

        SortedSet<String> idsSorted = new TreeSet<>();
        for (MessageID messageId : MessageID.values()) {
            idsSorted.add(messageId.getId());
        }
        sb.append("[[section-gen-messaging-overview-linklist]]\n");
        sb.append("====== List of all messages\n");
        for (String id : idsSorted) {
            sb.append("- <<" + createMessagingLinkId(id) + "," + id + ">>\n");
        }
        sb.append("\n\n");
        for (String id : idsSorted) {
            String messagePlantUmlFileName = createGeneratedPlantUmlFileName(MessageID.valueOf(id));
            sb.append("[[" + createMessagingLinkId(id) + "]]\n");
            sb.append("===== Message " + id + "\n");
            sb.append("plantuml::diagrams/gen/" + messagePlantUmlFileName + "[format=svg, alt=\"Sequence diagram of messaging " + id + "\"]\n");
            sb.append("\n");
            sb.append("include::message2usecases_" + createMessagingLinkId(id) + ".adoc[]\n");
        }
        writer.writeTextToFile(messagingFile, sb.toString());
    }

    public static String createMessagingLinkId(MessageID messageId) {
        return createMessagingLinkId(messageId.name());
    }

    public static String createMessagingLinkId(String messageId) {
        return "section-gen-messaging-" + messageId.toLowerCase();
    }

    private String createGeneratedPlantUmlFileName(MessageID messageId) {
        return "gen_domain_messaging_" + messageId.name().toLowerCase() + ".plantuml";
    }

    private void generateMessagingPlantumlFile(File diagramsGenFolder, DomainMessagingModel model, String title, String targetFileName,
            MessageID[] messageIdsToInspect) throws IOException {
        String generatedPlantuml = domainMessagingModelPlantUMLGenerator.generate(model, title, messageIdsToInspect, true);
        File targetFile = new File(diagramsGenFolder, targetFileName);
        writer.writeTextToFile(targetFile, generatedPlantuml);
    }

    public DomainMessagingModel createReducedClone(DomainMessagingModel originModel) {
        DomainMessagingModel newModel = new DomainMessagingModel();
        for (Domain domain : originModel.domains) {
            Domain newDomain = newModel.newDomain();
            newDomain.name = domain.name;
            DomainPart newSingleDomainPart = newDomain.newDomainPart();
            newSingleDomainPart.name = domain.name;
            for (DomainPart part : domain.domainParts) {
                newSingleDomainPart.involvedWithMessages.addAll(part.involvedWithMessages);
                newSingleDomainPart.receivingAsyncMessages.addAll(part.receivingAsyncMessages);
                newSingleDomainPart.recevingSyncMessages.addAll(part.recevingSyncMessages);
                newSingleDomainPart.sendingAsyncMessages.addAll(part.sendingAsyncMessages);
                newSingleDomainPart.sendingSyncMessageAnswers.addAll(part.sendingSyncMessageAnswers);
                newSingleDomainPart.sendingSyncMessages.addAll(part.sendingSyncMessages);
            }
            newDomain.domainParts.add(newSingleDomainPart);
            newModel.domains.add(newDomain);
        }
        return newModel;
    }
}
