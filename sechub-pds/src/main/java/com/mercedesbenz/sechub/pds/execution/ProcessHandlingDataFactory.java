// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;
import com.mercedesbenz.sechub.pds.job.PDSJobConfigurationSupport;

@Component
public class ProcessHandlingDataFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessHandlingDataFactory.class);

    @Autowired
    PDSServerConfigurationService serverConfigurationService;

    public ProductCancellationProcessHandlingData createForCancelOperation(PDSJobConfiguration configuration) {

        PDSJobConfigurationSupport configurationSupport = new PDSJobConfigurationSupport(configuration);

        int millisecondsToWaitForNextCheck = calculateMillisecondsToWaitForNextCheck(configurationSupport);
        int secondsToWaitForProcess = calculateSecondsToWaitForProcess(configurationSupport);

        ProductCancellationProcessHandlingData data = new ProductCancellationProcessHandlingData(secondsToWaitForProcess, millisecondsToWaitForNextCheck);

        return data;
    }

    public ProductLaunchProcessHandlingData createForLaunchOperation(PDSJobConfiguration configuration) {

        PDSJobConfigurationSupport configurationSupport = new PDSJobConfigurationSupport(configuration);

        int minutesToWaitBeforeProductTimeout = calculateMinutesToWaitForProduct(configurationSupport);

        ProductLaunchProcessHandlingData data = new ProductLaunchProcessHandlingData(minutesToWaitBeforeProductTimeout);

        return data;
    }

    private int calculateMinutesToWaitForProduct(PDSJobConfigurationSupport configurationSupport) {
        int systemWideMinutesToWaitForProduct = serverConfigurationService.getMinutesToWaitForProduct();

        int maxCheck = serverConfigurationService.getMaximumConfigurableMinutesToWaitForProduct();
        int minCheck = serverConfigurationService.getMinimumConfigurableMinutesToWaitForProduct();

        int calculatedMinutesToWaitForProduct = configurationSupport.getMinutesToWaitBeforeProductTimeOut(systemWideMinutesToWaitForProduct);

        if (calculatedMinutesToWaitForProduct > maxCheck) {
            int wrongValue = calculatedMinutesToWaitForProduct;
            calculatedMinutesToWaitForProduct = maxCheck;

            LOG.warn(
                    "Configuration wants to wait for {} minutes for the product. But this exceeds our accepted maximum of {} minutes! Will set fallback to {} minutes.",
                    wrongValue, maxCheck, calculatedMinutesToWaitForProduct);
        }
        if (calculatedMinutesToWaitForProduct < minCheck) {
            int wrongValue = calculatedMinutesToWaitForProduct;
            calculatedMinutesToWaitForProduct = minCheck;

            LOG.warn(
                    "Configuration wants to wait for {} minutes for the product. But this is lower than our accepted minimum of {} minutes! Will set fallback to {} minute(s).",
                    wrongValue, minCheck, calculatedMinutesToWaitForProduct);
        }
        return calculatedMinutesToWaitForProduct;

    }

    private int calculateMillisecondsToWaitForNextCheck(PDSJobConfigurationSupport configuration) {
        int maxCheck = MAXIMUM_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK;
        int minCheck = MINIMUM_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK;

        int millisecondsToWaitForNextCheck = configuration.getMillisecondsToWaitForNextCheck();
        if (millisecondsToWaitForNextCheck > maxCheck) {
            /* maybe wrong configured, or script has changed the event... */
            LOG.warn(
                    "Cancel event wants to check each {} milliseconds. But this exceeds our accepted maximum of {} seconds! Will set fallback to {} milliseconds.",
                    millisecondsToWaitForNextCheck, maxCheck, maxCheck);
            millisecondsToWaitForNextCheck = maxCheck;
        }
        if (millisecondsToWaitForNextCheck < minCheck) {
            /* maybe wrong configured, or script has changed the event... */
            LOG.warn(
                    "Cancel event wants to check each {} milliseconds. But this is lower than our accepted minimum of {} milliseconds! Will fallback to {} seconds.",
                    millisecondsToWaitForNextCheck, minCheck, minCheck);
            millisecondsToWaitForNextCheck = minCheck;
        }
        return millisecondsToWaitForNextCheck;
    }

    private int calculateSecondsToWaitForProcess(PDSJobConfigurationSupport configuration) {
        int maxWait = MAXIMUM_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION;
        int minWait = NO_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION;

        int secondsToWaitForProcess = configuration.getSecondsToWaitForProcess();
        if (secondsToWaitForProcess > maxWait) {
            /* maybe wrong configured, or script has changed the event... */
            LOG.warn("Cancel event wants to wait for {} seconds. But this exceeds accepted maximum of {} seconds!", secondsToWaitForProcess, maxWait);
            secondsToWaitForProcess = maxWait;
        }
        if (secondsToWaitForProcess < minWait) {
            LOG.warn("Cancel event wants to wait for {} seconds. But this is lower than accepted minimum of {} seconds!");
            secondsToWaitForProcess = minWait;
        }
        return secondsToWaitForProcess;
    }
}
