// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.wrapper.secret.validator.execution.SecretValidatorExecutionService;
import com.mercedesbenz.sechub.wrapper.secret.validator.properties.SecretValidatorPDSJobResult;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

@Profile("!test")
@Component
public class SecretValidatorCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorCLI.class);

    private final SecretValidatorExecutionService executionService;
    private final SecretValidatorPDSJobResult pdsJobResult;

    public SecretValidatorCLI(SecretValidatorExecutionService executionService, SecretValidatorPDSJobResult pdsJobResult) {
        this.executionService = executionService;
        this.pdsJobResult = pdsJobResult;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Secret validator starting");
        TextFileWriter fileWriter = new TextFileWriter();
        try {
            SarifSchema210 report = executionService.execute();

            String json = JSONConverter.get().toJSON(report, true);
            fileWriter.writeTextToFile(pdsJobResult.getFile(), json, true);
        } catch (Exception e) {
            LOG.error("Execution failed!", e);
            System.exit(1);
        }
    }

}
