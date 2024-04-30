package com.mercedesbenz.sechub.wrapper.prepare.cli;

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
            result = getAdapterExecutionResultFailed(e);
            LOG.error("Preparation of remote data has failed.", e);
        }
        storeResultOrFail(result);
    }

    private static AdapterExecutionResult getAdapterExecutionResultFailed(Exception e) {
        PrepareResult prepareResult = new PrepareResult(PrepareStatus.FAILED);
        Collection<SecHubMessage> messages = new ArrayList<>();
        messages.add(new SecHubMessage(SecHubMessageType.ERROR, e.getMessage()));
        return new AdapterExecutionResult(prepareResult.toString(), messages);
    }

    private void storeResultOrFail(AdapterExecutionResult result) {
        try {
            storageService.store(result);
        } catch (Exception e) {
            LOG.error("Storing preparation result has failed.", e);
            System.exit(1);
        }
    }
}
