package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.beust.jcommander.JCommander;
import com.mercedesbenz.sechub.wrapper.checkmarx.Console;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperScanService;

@Component
public class CheckmarxWrapperCLIComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperCLIComponent.class);

    @Autowired
    CheckmarxWrapperScanService scanService;

    @Bean
    public CommandLineRunner createRunner() {
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
                scanService.startScan();
            } catch (Exception e) {
                Console.LOG.error("Execution failed - {}", e.getMessage());
                LOG.error("Execution failed", e);

                System.exit(2);
            }

        }

    }

}
