// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.cli;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareResult;
import com.mercedesbenz.sechub.commons.core.prepare.PrepareStatus;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperPreparationService;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperResultService;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;
import com.mercedesbenz.sechub.wrapper.prepare.upload.PrepareWrapperUploadException;

@Component
public class PrepareWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperCLI.class);

    private final PrepareWrapperPreparationService preparationService;

    private final PrepareWrapperResultService resultService;

    private final PrepareWrapperDefaultExitService exitService;

    public PrepareWrapperCLI(PrepareWrapperPreparationService preparationService, PrepareWrapperResultService resultService,
            PrepareWrapperDefaultExitService exitService) {
        this.preparationService = preparationService;
        this.resultService = resultService;
        this.exitService = exitService;
    }

    @Override
    public void run(String... args) {
        LOG.debug("Prepare wrapper is starting.");
        AdapterExecutionResult result;
        boolean isPreparationSuccessful = true;

        try {
            result = preparationService.startPreparation();

        } catch (PrepareWrapperUploadException e) {
            LOG.error("Preparation of remote data has failed. Could not upload data to shared storage. ExitCode: {}", e.getExitCode(), e);
            result = getAdapterExecutionResultFailed("Could not prepare remote data, because of an internal storage error.");
            isPreparationSuccessful = false;

        } catch (PrepareWrapperUsageException e) {
            /* Usage exception messages will be added to sechub messages */
            LOG.error("Preparation of remote data has failed, because of wrong usage. ExitCode: {}", e.getExitCode(), e);
            result = getAdapterExecutionResultFailed(e.getMessage());
            isPreparationSuccessful = false;

        } catch (Exception e) {
            LOG.error("Preparation of remote data has failed.", e);
            result = getAdapterExecutionResultFailed("Could not prepare remote data, because of an internal error.");
            isPreparationSuccessful = false;
        }

        storeResultOrFail(result);

        if (!isPreparationSuccessful) {
            LOG.error("Prepare wrapper has finished with errors.");
            exitService.exitWithCode(1);
        } else {
            LOG.info("Prepare wrapper has finished successfully.");
        }
    }

    private static AdapterExecutionResult getAdapterExecutionResultFailed(String message) {
        PrepareResult prepareResult = new PrepareResult(PrepareStatus.FAILED);
        Collection<SecHubMessage> messages = new ArrayList<>();
        messages.add(new SecHubMessage(SecHubMessageType.ERROR, message));
        return new AdapterExecutionResult(prepareResult.toString(), messages);
    }

    private void storeResultOrFail(AdapterExecutionResult result) {
        try {
            resultService.store(result);
        } catch (Exception e) {
            LOG.error("Storing preparation result has failed.", e);
            System.exit(1);
        }
    }
}