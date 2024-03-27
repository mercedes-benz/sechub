package com.mercedesbenz.sechub.wrapper.prepare.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperPreparationService;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperStorageService;

@Component
public class PrepareWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperCLI.class);

    @Autowired
    PrepareWrapperPreparationService preparationService;

    @Autowired
    PrepareWrapperStorageService storageService;

    @Autowired
    PrepareWrapperResultStatus status;

    @Override
    public void run(String... args) {
        LOG.debug("Prepare wrapper starting");
        AdapterExecutionResult result;
        try {
            result = preparationService.startPreparation();
        } catch (Exception e) {
            result = new AdapterExecutionResult(status.getSTATUS_FAILED());
            LOG.error("Execution failed", e);
        }
        try {
            storageService.store(result);
        } catch (IOException e) {
            LOG.error("Result storing failed", e);
            System.exit(2);
        }

    }
}
