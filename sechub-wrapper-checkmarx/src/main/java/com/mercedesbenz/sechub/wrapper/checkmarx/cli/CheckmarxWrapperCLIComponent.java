package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.beust.jcommander.JCommander;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConfig;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.wrapper.checkmarx.CheckmarxWrapperEnvironment;
import com.mercedesbenz.sechub.wrapper.checkmarx.Console;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperContext;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperContextFactory;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperScanService;

@Component
public class CheckmarxWrapperCLIComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperCLIComponent.class);

    @Autowired
    CheckmarxWrapperScanService scanService;

    @Autowired
    CheckmarxWrapperEnvironment environment;

    @Autowired
    CodeScanPathCollector codeScanPathCollector;

    @Autowired
    CheckmarxWrapperContextFactory factory;

    @Bean
    public CommandLineRunner initialIntegrationTestAdmin() {
        return new CheckmarxWrapperCommandLineRunner();
    }

    public class CheckmarxWrapperCommandLineRunner implements CommandLineRunner {

        @Override
        public void run(String... args) throws Exception {
            LOG.info("Checkmarx wrapper starting");

            CheckmarxWrapperCLIParameters arguments = new CheckmarxWrapperCLIParameters();

            JCommander commander = JCommander.newBuilder().programName("checkmarxWrapper").addObject(arguments).build();

            try {
                commander.parse(args);
            } catch (Exception e) {
                Console.LOG.error("Unsupported arguments:" + e.getMessage());
                commander.usage();
                return;
            }

            if (arguments.isHelpOutputNecessary()) {
                commander.usage();
                return;
            }

            try {
                /* action */
                switch (arguments.getAction()) {
                case "start":
                    scanService.startScan(createConfig());
                    break;
                default:
                    failWithUnsupportedArgumentCombination("Action " + arguments.getAction() + " is not accepted", commander);
                }
            } catch (Exception e) {
                Console.LOG.error("Execution failed - {}", e.getMessage());
                LOG.error("Execution failed", e);

                System.exit(2);
            }

        }

    }

    private CheckmarxAdapterConfig createConfig() {
        /* @formatter:off */


        CheckmarxWrapperContext context = factory.create(environment);

        @SuppressWarnings("deprecation")
        CheckmarxAdapterConfig checkMarxConfig = CheckmarxConfig.builder().
//                configure(new SecHubAdapterOptionsBuilderStrategy(data, getScanType())).
                setTrustAllCertificates(environment.isTrustAllCertificatesEnabled()).
                setUser(environment.getUser()).
                setPasswordOrAPIToken(environment.getCheckmarxPassword()).
                setProductBaseUrl(environment.getCheckmarxProductBaseURL()).

                setAlwaysFullScan(environment.isAlwaysFullScanEnabled()).
                setTimeToWaitForNextCheckOperationInMinutes(environment.getScanResultCheckPeriodInMinutes()).
                setTimeOutInMinutes(environment.getScanResultCheckTimeOutInMinutes()).
                setFileSystemSourceFolders(context.createCodeUploadFileSystemFolders()). // to support mocked Checkmarx adapters we MUST use still the deprecated method!
                setSourceCodeZipFileInputStream(context.createSourceCodeZipFileInputStream()).
                setTeamIdForNewProjects(context.getTeamIdForNewProjects()).
                setClientSecret(environment.getClientSecret()).
                setEngineConfigurationName(environment.getEngineConfigurationName()).
                setPresetIdForNewProjects(context.getPresetIdForNewProjects()).
                setProjectId(context.getProjectId()).
                setTraceID(environment.getSecHubJobUUID()).
                build();
            /* @formatter:on */

        return CheckmarxConfig.builder().setAlwaysFullScan(false).

                build();

        /* @formatter:on */
    }

    private void failWithUnsupportedArgumentCombination(String message, JCommander commander) {
        Console.LOG.info("Given argument combination not valid - reason:" + message);
        commander.usage();
        System.exit(1);

    }
}
