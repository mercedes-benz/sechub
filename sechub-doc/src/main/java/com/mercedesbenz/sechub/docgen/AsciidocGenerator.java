// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.mercedesbenz.sechub.docgen.messaging.DomainMessagingFilesGenerator;
import com.mercedesbenz.sechub.docgen.messaging.DomainMessagingModel;
import com.mercedesbenz.sechub.docgen.messaging.UseCaseEventMessageLinkAsciidocGenerator;
import com.mercedesbenz.sechub.docgen.messaging.UseCaseEventOverviewPlantUmlGenerator;
import com.mercedesbenz.sechub.docgen.pds.CheckmarxWrapperDocumentationGenerator;
import com.mercedesbenz.sechub.docgen.spring.ScheduleDescriptionGenerator;
import com.mercedesbenz.sechub.docgen.spring.SpringProfilesPlantumlGenerator;
import com.mercedesbenz.sechub.docgen.spring.SpringProfilesPlantumlGenerator.SpringProfileGenoConfig;
import com.mercedesbenz.sechub.docgen.spring.SystemPropertiesDescriptionGenerator;
import com.mercedesbenz.sechub.docgen.spring.SystemPropertiesJavaLaunchExampleGenerator;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseAsciiDocGenerator;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseModel;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseRestDocModel;
import com.mercedesbenz.sechub.docgen.usecase.UseCaseRestDocModelAsciiDocGenerator;
import com.mercedesbenz.sechub.docgen.util.ClasspathDataCollector;
import com.mercedesbenz.sechub.docgen.util.TextFileWriter;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseIdentifier;

public class AsciidocGenerator implements Generator {

    ClasspathDataCollector collector;
    TextFileWriter writer = new TextFileWriter();

    /* ---------------------------------- */
    /* ----- GENERATORS ----------------- */
    /* ---------------------------------- */

    /* Common (PDS+SecHub) */
    SystemPropertiesDescriptionGenerator propertiesGenerator = new SystemPropertiesDescriptionGenerator();
    UseCaseAsciiDocGenerator useCaseModelAsciiDocGenerator = new UseCaseAsciiDocGenerator();

    /* SecHub */
    ClientDocFilesGenerator clientDocFilesGenerator = new ClientDocFilesGenerator();

    ExampleJSONGenerator exampleJSONGenerator = new ExampleJSONGenerator();

    SystemPropertiesJavaLaunchExampleGenerator javaLaunchExampleGenerator = new SystemPropertiesJavaLaunchExampleGenerator();
    ScheduleDescriptionGenerator scheduleDescriptionGenerator = new ScheduleDescriptionGenerator();
    UseCaseRestDocModelAsciiDocGenerator useCaseRestDocModelAsciiDocGenerator = new UseCaseRestDocModelAsciiDocGenerator();
    DomainMessagingFilesGenerator domainMessagingFilesGenerator = new DomainMessagingFilesGenerator(writer);

    UseCaseEventOverviewPlantUmlGenerator usecaseEventOverviewGenerator;
    UseCaseEventMessageLinkAsciidocGenerator useCaseEventMessageLinkAsciidocGenerator;

    /* PDS */
    PDSExecutorConfigurationParameterDescriptionGenerator pdsExecutorConfigParameterGenerator = new PDSExecutorConfigurationParameterDescriptionGenerator();
    CheckmarxWrapperDocumentationGenerator checkmarxWrapperEnvGenerator = new CheckmarxWrapperDocumentationGenerator();

    public static void main(String[] args) throws Exception {
        output(">AsciidocGenerator starting");

        if (args.length != 1) {
            throw new IllegalArgumentException("call with target gen folder as first parameter only!");
        }
        String path = args[0];

        AsciidocGenerator asciidocGenerator = new AsciidocGenerator();
        asciidocGenerator.generate(path);

    }

    public AsciidocGenerator() {
        initLogging();
    }

    private void initLogging() {
        /* do some logging setup stuff to avoid unnecessary logs */
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO); // avoid warnings
        Logger reflections = (Logger) LoggerFactory.getLogger("org.reflections");
        reflections.setLevel(Level.ERROR);
    }

    static class GenContext {
        File documentsGenFolder;
        File documentsFolder;
        File diagramsFolder;
        File diagramsGenFolder;
        File systemProperitesFile;
        File pdsSystemProperitesFile;
        File pdsPDSExecutorConfigParametersFile;
        File javaLaunchExampleFile;
        File scheduleDescriptionFile;
        File specialMockValuePropertiesFile;
        File messagingFile;
    }

    private void generate(String path) throws IOException {
        GenContext context = initContext(path);

        /* SECHUB */
        generateUseCaseEventData(context); // must be done initial

        generateExampleFiles(context);
        generateClientParts(context);
        fetchMustBeDocumentParts();
        generateSystemPropertiesDescription(context);
        generateJavaLaunchExample(context);
        generateScheduleDescription(context);
        generateMockPropertiesDescription(context);
        generateMessagingFiles(context);
        generateUseCaseFiles(context);
        generateProfilesOverview(context);

        /* PDS */
        generatePDSUseCaseFiles(context);
        generatePDSSystemPropertiesDescription(context);
        generatePDSExecutorConfigurationParamters(context);

        /* PDS-solution */
        generateCheckmarxPDSSolutionParts(context);
    }

    private void generateUseCaseEventData(GenContext context) throws IOException {
        File jsonEventDataFolder = new File("./../sechub-integrationtest/build/test-results/event-trace");
        usecaseEventOverviewGenerator = new UseCaseEventOverviewPlantUmlGenerator(jsonEventDataFolder, context.diagramsGenFolder);
        usecaseEventOverviewGenerator.generateAndRememberUsecaseNamesToMessageIdMapping();
        Map<UseCaseIdentifier, Set<String>> useCasetoMessageIdsMap = usecaseEventOverviewGenerator.getUsecaseNameToMessageIdsMap();

        useCaseEventMessageLinkAsciidocGenerator = new UseCaseEventMessageLinkAsciidocGenerator(useCasetoMessageIdsMap, context.documentsGenFolder);
        useCaseEventMessageLinkAsciidocGenerator.generate();
    }

    private GenContext initContext(String path) {
        GenContext context = new GenContext();

        context.documentsGenFolder = new File(path);
        context.documentsFolder = context.documentsGenFolder.getParentFile();
        context.diagramsFolder = new File(context.documentsFolder.getParentFile(), "diagrams");
        context.diagramsGenFolder = new File(context.diagramsFolder, "gen");

        context.systemProperitesFile = createSystemProperyTargetFile(context.documentsGenFolder);
        context.pdsSystemProperitesFile = createPDSSystemProperyTargetFile(context.documentsGenFolder);
        context.pdsPDSExecutorConfigParametersFile = createPDSExecutorConfigurationParametersTargetFile(context.documentsGenFolder);

        context.javaLaunchExampleFile = createJavaLaunchExampleTargetFile(context.documentsGenFolder);
        context.scheduleDescriptionFile = createScheduleDescriptionTargetFile(context.documentsGenFolder);
        context.specialMockValuePropertiesFile = createSpecialMockConfigurationPropertiesTargetFile(context.documentsGenFolder);
        context.messagingFile = createMessagingTargetFile(context.documentsGenFolder);
        return context;
    }

    private void generateCheckmarxPDSSolutionParts(GenContext context) throws IOException {
        String table = checkmarxWrapperEnvGenerator.generateEnvironmentAndJobParameterTable();

        File clientGenDocFolder = new File(context.documentsGenFolder, "pds-solutions");
        File targetFile = new File(clientGenDocFolder, "gen_checkmarx_wrapper_env_and_job_parameter_table.adoc");
        writer.save(targetFile, table);
    }

    private void generateClientParts(GenContext context) throws IOException {

        String defaultZipAllowedFilePatternsTable = clientDocFilesGenerator.generateDefaultZipAllowedFilePatternsTable();
        File clientGenDocFolder = new File(context.documentsGenFolder, "client");
        File targetFile = new File(clientGenDocFolder, "gen_table_default_zip_allowed_file_patterns.adoc");
        writer.save(targetFile, defaultZipAllowedFilePatternsTable);
    }

    private void generateExampleFiles(GenContext context) throws IOException {
        generateExample("project_mockdata_config1.json", context.documentsGenFolder, exampleJSONGenerator.generateScanProjectMockDataConfiguration1());
        generateExample("project_mockdata_config2.json", context.documentsGenFolder, exampleJSONGenerator.generateScanProjectMockDataConfiguration2());

    }

    private void generateExample(String endingfileName, File documentsGenFolder, String content) throws IOException {
        File examplesFolder = new File(documentsGenFolder, "examples");
        File targetFile = new File(examplesFolder, "gen_example_" + endingfileName);
        writer.save(targetFile, content);
    }

    private void generateProfilesOverview(GenContext context) throws IOException {
        SpringProfilesPlantumlGenerator geno = new SpringProfilesPlantumlGenerator();

        /* generate overview */
        generateSpringProfilePlantUML(context.diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().build());

        generateSpringProfilePlantUML(context.diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().filterToProfile("prod").build());
        generateSpringProfilePlantUML(context.diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().filterToProfile("dev")
                .satelites("mocked_notifications", "mocked_products", "real_products", "h2", "postgres").build());
        generateSpringProfilePlantUML(context.diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().filterToProfile("integrationtest")
                .satelites("mocked_products", "real_products", "h2", "postgres").build());

    }

    private void generateSpringProfilePlantUML(File diagramsGenFolder, SpringProfilesPlantumlGenerator geno, SpringProfileGenoConfig config)
            throws IOException {
        String addition = config.getFilteredProfile();
        if (addition != null) {
            addition = "-" + addition;
        } else {
            addition = "";
        }
        String text = geno.generate(config);
        File targetFile = new File(diagramsGenFolder, "gen_springprofiles" + addition + ".puml");
        writer.save(targetFile, text);
    }

    private void generateMessagingFiles(GenContext context) throws IOException {
        DomainMessagingModel model = getCollector().fetchDomainMessagingModel();
        domainMessagingFilesGenerator.generateMessagingFiles(context.messagingFile, context.diagramsGenFolder, model);
    }

    private void generateUseCaseFiles(GenContext context) throws IOException {
        UseCaseModel model = getCollector().fetchUseCaseModel();
        UseCaseRestDocModel restDocModel = getCollector().fetchUseCaseRestDocModel(model);

        String useCaseAsciidoc = useCaseModelAsciiDocGenerator.generateAsciidoc(model, context.diagramsGenFolder);

        File targetFile = new File(context.documentsGenFolder, "gen_usecases.adoc");
        writer.save(targetFile, useCaseAsciidoc);

        String usecaseRestDoc = useCaseRestDocModelAsciiDocGenerator.generateAsciidoc(writer, restDocModel, true, UseCaseIdentifier.values());
        File targetFile2 = new File(context.documentsGenFolder, "gen_uc_restdoc.adoc");
        writer.save(targetFile2, usecaseRestDoc);
    }

    private void generatePDSUseCaseFiles(GenContext context) throws IOException {
        UseCaseModel model = getCollector().fetchPDSUseCaseModel();

        String useCaseAsciidoc = useCaseModelAsciiDocGenerator.generateAsciidoc(model, context.diagramsGenFolder, false, false);

        File targetFile = new File(context.documentsGenFolder, "gen_pds-usecases.adoc");
        writer.save(targetFile, useCaseAsciidoc);
    }

    static void output(String text) {
        // We just do an output on console for build tool - e.g gradle...
        /* NOSONAR */System.out.println(text);
    }

    static File createScheduleDescriptionTargetFile(File genFolder) {
        return new File(genFolder, "gen_scheduling.adoc");
    }

    static File createSystemProperyTargetFile(File genFolder) {
        return new File(genFolder, "gen_systemproperties.adoc");
    }

    static File createPDSSystemProperyTargetFile(File genFolder) {
        return new File(genFolder, "gen_pds_systemproperties.adoc");
    }

    static File createJavaLaunchExampleTargetFile(File genFolder) {
        return new File(genFolder, "gen_javalaunchexample.adoc");
    }

    static File createMessagingTargetFile(File genFolder) {
        return new File(genFolder, "gen_messaging.adoc");
    }

    static File createSpecialMockConfigurationPropertiesTargetFile(File genFolder) {
        return new File(genFolder, "gen_mockadapterproperties.adoc");
    }

    private static File createPDSExecutorConfigurationParametersTargetFile(File genFolder) {
        return new File(genFolder, "gen_pds_executor_config_parameters.adoc");
    }

    /**
     * Just an extra method to seperate the fetch mechanism from others
     */
    public void fetchMustBeDocumentParts() {
        getCollector().fetchMustBeDocumentParts();
    }

    public void fetchDomainMessagingParts() {
        getCollector().fetchDomainMessagingModel();
    }

    public void generateSystemPropertiesDescription(GenContext context) throws IOException {
        String text = propertiesGenerator.generate(getCollector().fetchMustBeDocumentParts());
        writer.save(context.systemProperitesFile, text);
    }

    public void generatePDSSystemPropertiesDescription(GenContext context) throws IOException {
        String text = propertiesGenerator.generate(getCollector().fetchPDSMustBeDocumentParts());
        writer.save(context.pdsSystemProperitesFile, text);
    }

    private void generatePDSExecutorConfigurationParamters(GenContext context) throws IOException {
        String text = pdsExecutorConfigParameterGenerator.generatePDSExecutorConfigurationParamters(context.pdsPDSExecutorConfigParametersFile);
        writer.save(context.pdsPDSExecutorConfigParametersFile, text);
    }

    public void generateJavaLaunchExample(GenContext context) throws IOException {
        String text = javaLaunchExampleGenerator.generate(getCollector().fetchMustBeDocumentParts());
        writer.save(context.javaLaunchExampleFile, text);
    }

    public void generateScheduleDescription(GenContext context) throws IOException {
        String text = scheduleDescriptionGenerator.generate(getCollector());
        writer.save(context.scheduleDescriptionFile, text);
    }

    private void generateMockPropertiesDescription(GenContext context) throws IOException {
        String text = propertiesGenerator.generate(getCollector().fetchMockAdapterSpringValueDocumentationParts());
        writer.save(context.specialMockValuePropertiesFile, text);
    }

    private ClasspathDataCollector getCollector() {
        if (collector == null) {
            collector = new ClasspathDataCollector();
        }
        return collector;
    }

}
