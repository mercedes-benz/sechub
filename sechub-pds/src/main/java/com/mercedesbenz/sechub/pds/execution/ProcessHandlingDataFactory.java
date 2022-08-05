package com.mercedesbenz.sechub.pds.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHandlingDataFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessHandlingDataFactory.class);

    private static final int MAXIMUM_ACCEPTED_SECONDS_TO_WAIT_FOR_PROCESS_ON_CANCEL = 60 * 10; // ten minutes max
    private static final int DEFAULT_0_SECONDS_TO_WAIT_FOR_PROCESS_ON_CANCEL = 0;

    private static int MAXIMUM_MILLISECONDS_FOR_CHECK_INTERVAL = 5000; // 5 seconds
    private static int MINIMUM_MILLISECONDS_FOR_CHECK_INTERVAL = 300;

    public ProcessHandlingData createForCancelOperation(ExecutionEventData cancelEventData) {
        ProcessHandlingData data = new ProcessHandlingData();
        data.millisecondsToWaitForNextCheck = calculateMillisecondsToWaitForNextCheck(cancelEventData);
        data.secondsToWaitForProcess = calculateSecondsToWaitForProcess(cancelEventData);

        return data;
    }

    private int calculateMillisecondsToWaitForNextCheck(ExecutionEventData cancelEventData) {
        int millisecondsToWaitForNextCheck = cancelEventData.getDetail(ExecutionEventDetailIdentifier.CANCEL_REQUEST_MILLSECONDS_FOR_CHECK_INTERVAL,
                MINIMUM_MILLISECONDS_FOR_CHECK_INTERVAL);
        if (millisecondsToWaitForNextCheck > MAXIMUM_MILLISECONDS_FOR_CHECK_INTERVAL) {
            /* maybe wrong configured, or script has changed the event... */
            LOG.warn("Cancel event wants to check each {} milliseconds. But this exceeds our accepted maximum of {} seconds!s", millisecondsToWaitForNextCheck,
                    MAXIMUM_MILLISECONDS_FOR_CHECK_INTERVAL);
            millisecondsToWaitForNextCheck = MAXIMUM_MILLISECONDS_FOR_CHECK_INTERVAL;
        }
        if (millisecondsToWaitForNextCheck < MINIMUM_MILLISECONDS_FOR_CHECK_INTERVAL) {
            /* maybe wrong configured, or script has changed the event... */
            LOG.warn("Cancel event wants to check each {} milliseconds. But this is lower than our accepted minimum of {} milliseconds!s",
                    millisecondsToWaitForNextCheck, MINIMUM_MILLISECONDS_FOR_CHECK_INTERVAL);
            millisecondsToWaitForNextCheck = MINIMUM_MILLISECONDS_FOR_CHECK_INTERVAL;
        }
        return millisecondsToWaitForNextCheck;
    }

    private int calculateSecondsToWaitForProcess(ExecutionEventData cancelEventData) {
        int secondsToWaitForProcess = cancelEventData.getDetail(ExecutionEventDetailIdentifier.CANCEL_REQUEST_SECONDS_TO_WAIT_FOR_PROCESS,
                DEFAULT_0_SECONDS_TO_WAIT_FOR_PROCESS_ON_CANCEL);
        if (secondsToWaitForProcess > MAXIMUM_ACCEPTED_SECONDS_TO_WAIT_FOR_PROCESS_ON_CANCEL) {
            /* maybe wrong configured, or script has changed the event... */
            LOG.warn("Cancel event wants to wait for {} seconds. But this exceeds our accepted maximum of {} seconds!s", secondsToWaitForProcess,
                    MAXIMUM_ACCEPTED_SECONDS_TO_WAIT_FOR_PROCESS_ON_CANCEL);
            secondsToWaitForProcess = MAXIMUM_ACCEPTED_SECONDS_TO_WAIT_FOR_PROCESS_ON_CANCEL;
        }
        if (secondsToWaitForProcess < 0) {
            LOG.warn("Cancel event wants to wait for {} seconds. But negative values are not allowed.");
            secondsToWaitForProcess = 0;
        }
        return secondsToWaitForProcess;
    }
}
