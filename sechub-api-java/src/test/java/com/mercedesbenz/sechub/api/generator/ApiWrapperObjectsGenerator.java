package com.mercedesbenz.sechub.api.generator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This generator does the following:
 *
 * <pre>
 * 1. Reads the the folders inside the generated open api model packages
 * 2. Defines some explicit definitions which parts shall be part of public api
 * 3. Generates an abstract wrapper class and a model class which inherits the generated class
 * </pre>
 *
 * The model classes shall be editable by users and are only overridden when
 * explicitly defined. The abstract classes are always newly generated.
 *
 * @author Albert Tregnaghi
 *
 */
public class ApiWrapperObjectsGenerator {

    static final Logger LOG = LoggerFactory.getLogger(ApiWrapperObjectsGenerator.class);

    private ApiWrapperGenerationContext context;

    ApiWrapperObjectsGenerator() {
    }

    public void generateAndFormatWithSpotless(boolean overwritePublicModelFiles) throws Exception {
        init();

        PublicModelFileGenerator publicModelFileGenerator = new PublicModelFileGenerator(context);
        InternalAccessModelFileGenerator internalAccessModelFileGenerator = new InternalAccessModelFileGenerator(context);

        internalAccessModelFileGenerator.generate();
        publicModelFileGenerator.generate(overwritePublicModelFiles);

        runFormatter();
    }

    private void init() throws Exception {
        context = new ApiWrapperGenerationContext();

        /* ignore this, we have the origin model as dependency available */
        context.ignoreModel("OpenApiScanJob");
        context.ignoreModel("OpenApiScanJobCodeScan");
        context.ignoreModel("OpenApiScanJobCodeScanFileSystem");
        context.ignoreModel("OpenApiScanJobInfraScan");
        context.ignoreModel("OpenApiScanJobWebScan");
        context.ignoreModel("OpenApiScanJobWebScanLogin");
        context.ignoreModel("OpenApiScanJobWebScanLoginBasic");
        context.ignoreModel("OpenApiScanJobWebScanLoginForm");
        context.ignoreModel("OpenApiScanJobWebScanLoginFormScript");
        context.ignoreModel("OpenApiScanJobWebScanLoginFormScriptPagesInner");
        context.ignoreModel("OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner");
        context.ignoreModel("OpenApiScanJobWebScanMaxScanDuration");

        context.mapModel("OpenApiProject").markPublicAvailable();
        context.mapModel("OpenApiUserSignup").markPublicAvailable();
        context.mapModel("OpenApiExecutorConfigurationSetupJobParametersInner", "ExecutorConfigurationSetupJobParameter").markPublicAvailable();

        context.mapModel("OpenApiExecutionProfileCreate").markPublicAvailable();

        context.mapModel("OpenApiListOfSignupsInner", "OpenUserSignup").markPublicAvailable();

        context.mapModel("OpenApiExecutorConfiguration").markPublicAvailable();

        context.ignoreModel("OpenApiJobStatus"); // this will be implemented manually

        context.getCollector().collect();

    }

    private void runFormatter() throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder("./gradlew", ":sechub-api-java:spotlessApply", "--console=plain", "-Dsechub.build.stage=all");
        pb.directory(new File("./..")); // gradle root project
        pb.inheritIO();
        LOG.info("Start gradle process to format after generation");
        Process process = pb.start();
        LOG.info("Wait for gradle process");
        boolean exited = process.waitFor(2, TimeUnit.MINUTES);
        if (!exited) {
            throw new IllegalStateException("Timeout");
        }
        int exitCode = process.exitValue();

        if (exitCode != 0) {
            throw new IllegalStateException("Gradle build failed with exit code:" + exitCode);
        }

    }
}
