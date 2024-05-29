// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.wrapper.secret.validator.execution.SecretValidatorExecutionService;

@Component
public class SecretValidatorCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorCLI.class);

    @Autowired
    SecretValidatorExecutionService executionService;

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Secret validator starting");
        try {
            executionService.execute();
        } catch (Exception e) {
            LOG.error("Execution failed", e);
            System.exit(1);
        }
    }

}
