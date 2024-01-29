// SPDX-License-Identifier: MIT
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
 * 1. Reads the the folders inside the generated open API model packages
 * 2. Defines which parts shall be inside the public API
 * 3. Generates an internal access and a public model class which uses the internal access class
 * </pre>
 *
 * The model classes shall be editable by users and are only overridden when
 * explicitly defined. The internal access classes are always newly generated.
 *
 * Remark: For that, we use no inheritance, but instead composition (public
 * class calls internal access class) to be able to replace even method
 * signatures.
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
