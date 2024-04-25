package com.mercedesbenz.sechub.wrapper.prepare.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareStatus;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperPreparationService;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperStorageService;

@Component
public class PrepareWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperCLI.class);

    @Autowired
    PrepareWrapperPreparationService preparationService;

    @Autowired
    PrepareWrapperStorageService storageService;

    @Override
    public void run(String... args) {
        LOG.debug("Prepare wrapper starting");
        AdapterExecutionResult result;
        try {
            result = preparationService.startPreparation();
        } catch (Exception e) {
            PrepareResult prepareResult = new PrepareResult(PrepareStatus.FAILED);
            SecHubMessage message = new SecHubMessage(SecHubMessageType.ERROR, e.getMessage());
            Collection<SecHubMessage> messages = new ArrayList<>();
            messages.add(message);
            result = new AdapterExecutionResult(prepareResult.toString(), messages);
            LOG.error("Preparation of remote data has failed.", e);
            System.exit(1);
        }
        try {
            storageService.store(result);
        } catch (IOException e) {
            LOG.error("Storing preparation result has failed.", e);
            System.exit(2);
        }
    }
}
