// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.daimler.sechub.docgen.messaging.DomainMessagingFilesGenerator;
import com.daimler.sechub.docgen.messaging.DomainMessagingModel;
import com.daimler.sechub.docgen.messaging.UseCaseEventMessageLinkAsciidocGenerator;
import com.daimler.sechub.docgen.messaging.UseCaseEventOverviewPlantUmlGenerator;
import com.daimler.sechub.docgen.spring.ScheduleDescriptionGenerator;
import com.daimler.sechub.docgen.spring.SpringProfilesPlantumlGenerator;
import com.daimler.sechub.docgen.spring.SpringProfilesPlantumlGenerator.SpringProfileGenoConfig;
import com.daimler.sechub.docgen.spring.SystemPropertiesDescriptionGenerator;
import com.daimler.sechub.docgen.spring.SystemPropertiesJavaLaunchExampleGenerator;
import com.daimler.sechub.docgen.usecase.UseCaseAsciiDocGenerator;
import com.daimler.sechub.docgen.usecase.UseCaseModel;
import com.daimler.sechub.docgen.usecase.UseCaseRestDocModel;
import com.daimler.sechub.docgen.usecase.UseCaseRestDocModelAsciiDocGenerator;
import com.daimler.sechub.docgen.util.ClasspathDataCollector;
import com.daimler.sechub.docgen.util.TextFileWriter;
import com.daimler.sechub.sharedkernel.usecases.UseCaseIdentifier;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class AsciidocGenerator implements Generator {

	ClasspathDataCollector collector;
	SystemPropertiesDescriptionGenerator propertiesGenerator = new SystemPropertiesDescriptionGenerator();
	SystemPropertiesJavaLaunchExampleGenerator javaLaunchExampleGenerator = new SystemPropertiesJavaLaunchExampleGenerator();
	ScheduleDescriptionGenerator scheduleDescriptionGenerator = new ScheduleDescriptionGenerator();
	UseCaseAsciiDocGenerator useCaseModelAsciiDocGenerator = new UseCaseAsciiDocGenerator();
	UseCaseRestDocModelAsciiDocGenerator useCaseRestDocModelAsciiDocGenerator = new UseCaseRestDocModelAsciiDocGenerator();
	TextFileWriter writer = new TextFileWriter();
	DomainMessagingFilesGenerator domainMessagingFilesGenerator = new DomainMessagingFilesGenerator(writer);
	ExampleJSONGenerator exampleJSONGenerator = new ExampleJSONGenerator();
	ClientDocFilesGenerator clientDocFilesGenerator = new ClientDocFilesGenerator();

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("call with target gen folder as first parameter only!");
		}
		output(">AsciidocGenerator starting");

		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO); // avoid waringings from
		Logger reflections = (Logger)LoggerFactory.getLogger("org.reflections");
		reflections.setLevel(Level.ERROR);

		String path = args[0];
		File documentsGenFolder = new File(path);
		File documentsFolder = documentsGenFolder.getParentFile();
		File diagramsFolder = new File(documentsFolder.getParentFile(), "diagrams");
		File diagramsGenFolder = new File(diagramsFolder, "gen");

		File systemProperitesFile = createSystemProperyTargetFile(documentsGenFolder);
		File pdsSystemProperitesFile = createPDSSystemProperyTargetFile(documentsGenFolder);
		
		File javaLaunchExampleFile = createJavaLaunchExampleTargetFile(documentsGenFolder);
		File scheduleDescriptionFile = createScheduleDescriptionTargetFile(documentsGenFolder);
		File specialMockValuePropertiesFile = createSpecialMockConfigurationPropertiesTargetFile(documentsGenFolder);
		File messagingFile = createMessagingTargetFile(documentsGenFolder);

		/* ---------------------- */
		/* --- PRE-generation --- */
		/* ---------------------- */
		File jsonEventDataFolder = new File("./../sechub-integrationtest/build/test-results/event-trace");
		UseCaseEventOverviewPlantUmlGenerator usecaseEventOverviewGenerator = new UseCaseEventOverviewPlantUmlGenerator(jsonEventDataFolder, diagramsGenFolder);
		usecaseEventOverviewGenerator.generate();
		Map<UseCaseIdentifier, Set<String>> useCasetoMessageIdsMap = usecaseEventOverviewGenerator.getUsecaseNameToMessageIdsMap();
		
		UseCaseEventMessageLinkAsciidocGenerator useCaseEventMessageLinkAsciidocGenerator = new UseCaseEventMessageLinkAsciidocGenerator(useCasetoMessageIdsMap,documentsGenFolder);
		useCaseEventMessageLinkAsciidocGenerator.generate();
		
		/* ----------------------- */
        /* --- Main-generation --- */
        /* ----------------------- */
		AsciidocGenerator generator = new AsciidocGenerator();
		
		/* SECHUB */
		generator.generateExampleFiles(documentsGenFolder);
		generator.generateClientParts(documentsGenFolder);
		generator.fetchMustBeDocumentParts();
		generator.generateSystemPropertiesDescription(systemProperitesFile);
		generator.generateJavaLaunchExample(javaLaunchExampleFile);
		generator.generateScheduleDescription(scheduleDescriptionFile);
		generator.generateMockPropertiesDescription(specialMockValuePropertiesFile);
		generator.generateMessagingFiles(messagingFile, diagramsGenFolder);
		generator.generateUseCaseFiles(documentsGenFolder,diagramsGenFolder);
		generator.generateProfilesOverview(diagramsGenFolder);

		/* PDS*/
		generator.generatePDSUseCaseFiles(documentsGenFolder,diagramsGenFolder);
		generator.generatePDSSystemPropertiesDescription(pdsSystemProperitesFile);
		
	}


	private void generateClientParts(File documentsGenFolder) throws IOException{
	    
	    String defaultZipAllowedFilePatternsTable = clientDocFilesGenerator.generateDefaultZipAllowedFilePatternsTable();
	    File clientGenDocFolder = new File(documentsGenFolder,"client");
        File targetFile = new File(clientGenDocFolder, "gen_table_default_zip_allowed_file_patterns.adoc");
        writer.save(targetFile, defaultZipAllowedFilePatternsTable);
    }


    private void generateExampleFiles(File documentsGenFolder) throws IOException{
		generateExample("project_mockdata_config1.json", documentsGenFolder,exampleJSONGenerator.generateScanProjectMockDataConfiguration1());
		generateExample("project_mockdata_config2.json", documentsGenFolder,exampleJSONGenerator.generateScanProjectMockDataConfiguration2());
		
	}

	private void generateExample(String endingfileName, File documentsGenFolder, String content) throws IOException {
		File examplesFolder = new File(documentsGenFolder,"examples");
		File targetFile = new File(examplesFolder, "gen_example_"+endingfileName);
		writer.save(targetFile, content);
	}
	

	private void generateProfilesOverview(File diagramsGenFolder) throws IOException {
		SpringProfilesPlantumlGenerator geno = new SpringProfilesPlantumlGenerator();

		/* generate overview*/
		generateSpringProfilePlantUML(diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().build());

		generateSpringProfilePlantUML(diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().filterToProfile("prod").build());
		generateSpringProfilePlantUML(diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().filterToProfile("dev").satelites("mocked_notifications","mocked_products","real_products","h2","postgres").build());
		generateSpringProfilePlantUML(diagramsGenFolder, geno, SpringProfilesPlantumlGenerator.config().filterToProfile("integrationtest").satelites("mocked_products","real_products","h2","postgres").build());

	}

	private void generateSpringProfilePlantUML(File diagramsGenFolder, SpringProfilesPlantumlGenerator geno, SpringProfileGenoConfig config)
			throws IOException {
		String addition = config.getFilteredProfile();
		if (addition != null) {
			addition = "-" + addition;
		}else {
			addition="";
		}
		String text = geno.generate(config);
		File targetFile = new File(diagramsGenFolder, "gen_springprofiles" + addition + ".puml");
		writer.save(targetFile, text);
	}

	private void generateMessagingFiles(File messagingFile, File diagramsGenFolder) throws IOException {
		DomainMessagingModel model = getCollector().fetchDomainMessagingModel();
		domainMessagingFilesGenerator.generateMessagingFiles(messagingFile, diagramsGenFolder, model);
	}

	private void generateUseCaseFiles(File documentsGenFolder, File diagramsGenFolder) throws IOException {
		UseCaseModel model = getCollector().fetchUseCaseModel();
		UseCaseRestDocModel restDocModel = getCollector().fetchUseCaseRestDocModel(model);

		String useCaseAsciidoc = useCaseModelAsciiDocGenerator.generateAsciidoc(model,diagramsGenFolder);

		File targetFile = new File(documentsGenFolder, "gen_usecases.adoc");
		writer.save(targetFile, useCaseAsciidoc);

		String usecaseRestDoc = useCaseRestDocModelAsciiDocGenerator.generateAsciidoc(writer, restDocModel, true, UseCaseIdentifier.values());
		File targetFile2 = new File(documentsGenFolder, "gen_uc_restdoc.adoc");
		writer.save(targetFile2, usecaseRestDoc);

		/* @formatter:off */
		String usecaseRestDocUserDocumentation = useCaseRestDocModelAsciiDocGenerator.generateAsciidoc(writer, restDocModel, true,
				UseCaseIdentifier.UC_SIGNUP,
				UseCaseIdentifier.UC_USER_CREATES_JOB,
				UseCaseIdentifier.UC_USER_APPROVES_JOB,
				UseCaseIdentifier.UC_USER_GET_JOB_REPORT,
				UseCaseIdentifier.UC_USER_GET_JOB_STATUS);
		/* @formatter:on */
		File targetFile3 = new File(documentsGenFolder, "gen_uc_websiteumentation_restdoc.adoc");
		writer.save(targetFile3, usecaseRestDocUserDocumentation);

	}
	
	private void generatePDSUseCaseFiles(File documentsGenFolder, File diagramsGenFolder) throws IOException {
        UseCaseModel model = getCollector().fetchPDSUseCaseModel();

        String useCaseAsciidoc = useCaseModelAsciiDocGenerator.generateAsciidoc(model,diagramsGenFolder,false,false);

        File targetFile = new File(documentsGenFolder, "gen_pds-usecases.adoc");
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

	/**
	 * Just an extra method to seperate the fetch mechanism from others
	 */
	public void fetchMustBeDocumentParts() {
		getCollector().fetchMustBeDocumentParts();
	}

	public void fetchDomainMessagingParts() {
		getCollector().fetchDomainMessagingModel();
	}

	public void generateSystemPropertiesDescription(File targetFile) throws IOException {
		String text = propertiesGenerator.generate(getCollector().fetchMustBeDocumentParts());
		writer.save(targetFile, text);
	}
	
	public void generatePDSSystemPropertiesDescription(File targetFile) throws IOException {
        String text = propertiesGenerator.generate(getCollector().fetchPDSMustBeDocumentParts());
        writer.save(targetFile, text);
    }

	public void generateJavaLaunchExample(File targetFile) throws IOException {
		String text = javaLaunchExampleGenerator.generate(getCollector().fetchMustBeDocumentParts());
		writer.save(targetFile, text);
	}

	public void generateScheduleDescription(File targetFile) throws IOException {
		String text = scheduleDescriptionGenerator.generate(getCollector());
		writer.save(targetFile, text);
	}

	private void generateMockPropertiesDescription(File targetFile) throws IOException {
		String text = propertiesGenerator.generate(getCollector().fetchMockAdapterSpringValueDocumentationParts());
		writer.save(targetFile, text);
	}

	private ClasspathDataCollector getCollector() {
		if (collector == null) {
			collector = new ClasspathDataCollector();
		}
		return collector;
	}

}
