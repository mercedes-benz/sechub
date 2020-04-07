package com.daimler.sechub.docgen.messaging;

import static com.daimler.sechub.docgen.GeneratorConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.docgen.util.TextFileReader;
import com.daimler.sechub.docgen.util.TextFileWriter;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistory;
import com.daimler.sechub.sharedkernel.messaging.IntegrationTestEventHistoryInspection;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class UseCaseEventOverviewPlantUmlGenerator {
    

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseEventOverviewPlantUmlGenerator.class);
    private File folderToStartFrom;
    private TextFileReader reader;
    private TextFileWriter writer;
    private File outputFolder;

    public UseCaseEventOverviewPlantUmlGenerator(File jsonEventDataFolder, File outputFolder) {
        this.folderToStartFrom = jsonEventDataFolder;
        this.outputFolder = outputFolder;
        this.reader = new TextFileReader();
        this.writer = new TextFileWriter();
    }

    public void generate() {
        if (DEBUG) {
            LOG.info("start collecting by event-data from:" + folderToStartFrom);
        }

        File[] files = folderToStartFrom.listFiles();
        for (File file : files) {
            generateFile(file);
        }

    }

    private void generateFile(File jsonSourceFile) {
        String fileName = jsonSourceFile.getName();
        if (!fileName.endsWith(".json")) {
            return;
        }
        String usecaseId = fileName.substring(0, fileName.length() - 5);
        UseCaseIdentifier usecaseIdentifier;
        try {
            usecaseIdentifier = UseCaseIdentifier.valueOf(usecaseId.toUpperCase());
        } catch (RuntimeException e) {
            LOG.error("File not a usecase:" + jsonSourceFile);
            return;
        }
        LOG.info("Generate for usecase:{}, using json file: {}",usecaseIdentifier.name(),jsonSourceFile.getAbsolutePath());
        if (jsonSourceFile.getAbsolutePath().endsWith("uc_admin_enables_scheduler_job_processing.json")){
            System.out.println("got");
        }
        String json = reader.loadTextFile(jsonSourceFile);
        IntegrationTestEventHistory history = IntegrationTestEventHistory.fromJSONString(json);
        try {
            generate(history, usecaseIdentifier);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Wrong class names? Maybe you need to retrigger Integration tests to get new JSON files!", e);
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to write file!", e);
        }

    }

    private void generate(IntegrationTestEventHistory history, UseCaseIdentifier usecaseIdentifier) throws ClassNotFoundException, IOException {
        SortedMap<Integer, IntegrationTestEventHistoryInspection> map = history.getIdToInspectionMap();
        Set<Integer> keySet = map.keySet();

        PumlBuilder pb = new PumlBuilder();
        pb.add("@startuml");
        /* define actors etc. */
        pb.add("actor " + usecaseIdentifier.name());

        /* build domain list */
        Set<String> domains = new TreeSet<>();
        /* define actors etc. */
        for (Integer inspectionId : keySet) {
            IntegrationTestEventHistoryInspection inspection = map.get(inspectionId);
            Class<?> sender = Class.forName(inspection.getSenderClassName());
            domains.add(DomainUtil.createDomainName(sender));

            for (String receiverClass : inspection.getReceiverClassNames()) {
                Class<?> receiver = Class.forName(receiverClass);
                domains.add(DomainUtil.createDomainName(receiver));
            }
        }
        /* generate domain list as collections in puml */
        for (String domain : domains) {
            pb.add("collections " + domain);
        }

        /* link them */
        for (Integer identifier : keySet) {
            pb.add("== " + identifier.toString() + " ==");
            IntegrationTestEventHistoryInspection inspection = map.get(identifier);
            Class<?> sender = Class.forName(inspection.getSenderClassName());
            String senderDomain = DomainUtil.createDomainName(sender);
            if (identifier.intValue() == 0) {
                /* first one , so show usecase starts */
                pb.add(usecaseIdentifier.name() + " -> " + senderDomain + ": executed");
            }

            for (String receiverClass : inspection.getReceiverClassNames()) {
                Class<?> receiver = Class.forName(receiverClass);
                String receiverDomain = DomainUtil.createDomainName(receiver);
                pb.add(senderDomain + " --> " + receiverDomain + " : " + inspection.getEventId());
            }
        }
        pb.add("@enduml");

        /* write PLANTUML file */
        File targetFile = new File(outputFolder, createPlantumlFileSubPathByUsecase(usecaseIdentifier.name()));
        writer.save(targetFile, pb.getPlantUML());

    }

    public static String createPlantumlFileSubPathByUsecase(String usecaseIdentifierEnumName) {
        String lowerCase = usecaseIdentifierEnumName.toLowerCase();
        return "usecase-event-overview/" + lowerCase + "/event_overview_" + lowerCase + ".puml";
    }

    private class PumlBuilder {
        StringBuilder sb = new StringBuilder();

        public void add(String line) {
            sb.append(line);
            sb.append("\n");
        }

        public String getPlantUML() {
            return sb.toString();
        }
    }

}
