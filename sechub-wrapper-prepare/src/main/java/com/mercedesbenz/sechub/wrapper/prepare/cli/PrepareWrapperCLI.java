package com.mercedesbenz.sechub.wrapper.prepare.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperPreparationService;

@Component
public class PrepareWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperCLI.class);

    @Autowired
    PrepareWrapperPreparationService preparationService;

    @Override
    public void run(String... args) {
        LOG.debug("Prepare wrapper starting");
        try {
            preparationService.startPreparation();
        } catch (Exception e) {
            // TODO: 26.03.24 laura set prepare result to error
        }
    }
}
