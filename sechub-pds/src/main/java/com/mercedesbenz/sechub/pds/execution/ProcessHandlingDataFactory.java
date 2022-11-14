// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;
import com.mercedesbenz.sechub.pds.job.PDSJobConfigurationSupport;

public class ProcessHandlingDataFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessHandlingDataFactory.class);

    public ProcessHandlingData createForCancelOperation(PDSJobConfiguration configuration) {

        PDSJobConfigurationSupport configurationSupport = new PDSJobConfigurationSupport(configuration);

        int millisecondsToWaitForNextCheck = calculateMillisecondsToWaitForNextCheck(configurationSupport);
        int secondsToWaitForProcess = calculateSecondsToWaitForProcess(configurationSupport);
        ProcessHandlingData data = new ProcessHandlingData(secondsToWaitForProcess, millisecondsToWaitForNextCheck);

        return data;
    }

    private int calculateMillisecondsToWaitForNextCheck(PDSJobConfigurationSupport configuration) {
        int maxCheck = PDSDefaultParameterValueConstants.MAXIMUM_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK;
        int minCheck = PDSDefaultParameterValueConstants.MINIMUM_TIME_TO_WAIT_IN_MILLISECONDS_FOR_SCRIPT_CANCELLATION_CHECK;

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
        int maxWait = PDSDefaultParameterValueConstants.MAXIMUM_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION;
        int minWait = PDSDefaultParameterValueConstants.NO_TIME_TO_WAIT_IN_SECONDS_FOR_SCRIPT_CANCELLATION;

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
