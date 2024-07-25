// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

@Component
public abstract class AbstractToolWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractToolWrapper.class);
    private static final int defaultMinutesToWaitForProduct = PDSDefaultParameterValueConstants.DEFAULT_MINUTES_TO_WAIT_FOR_PRODUCT;

    @Value("${" + PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES + ":" + defaultMinutesToWaitForProduct + "}")
    private int pdsProductTimeoutMinutes;

    @Value("${" + KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS + ":-1}")
    private int pdsPrepareProcessTimeoutSeconds;

    protected void waitForProcessToFinish(ProcessAdapter processAdapter) throws IOException {

        LOG.debug("Wait for wrapper to finish process.");
        int seconds = calculateTimeoutSeconds();

        boolean exitDoneInTime = false;
        try {
            exitDoneInTime = processAdapter.waitFor(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("Wrapper for executed modul " + this.getClass().getSimpleName() + " could not finish process.", e);
        }

        if (!exitDoneInTime) {
            throw new IOException("Wrapper for executed modul " + this.getClass().getSimpleName() + " could not finish process. Waited "
                    + pdsPrepareProcessTimeoutSeconds + " seconds.");
        }

        if (processAdapter.exitValue() != 0) {
            LOG.error("Wrapper for executed modul {} process failed with exit code: {}", this.getClass().getSimpleName(), processAdapter.exitValue());
            throw new IOException(
                    "Wrapper for executed modul " + this.getClass().getSimpleName() + " process failed with exit code: " + processAdapter.exitValue());
        }
    }

    private int calculateTimeoutSeconds() {
        int pdsProductTimeoutInSeconds = pdsProductTimeoutMinutes * 60;

        if (pdsPrepareProcessTimeoutSeconds == -1) {
            return pdsProductTimeoutInSeconds;
        }

        if (pdsPrepareProcessTimeoutSeconds > pdsProductTimeoutInSeconds) {
            LOG.warn("Defined PDS product timeout is smaller than defined PDS prepare process timeout.");
            LOG.warn("Using PDS product timeout in Minutes: {}", pdsProductTimeoutMinutes);
            return pdsProductTimeoutInSeconds;
        }

        LOG.warn("Using custom timeout: {} seconds", pdsPrepareProcessTimeoutSeconds);
        LOG.warn("Defined PDS product timeout: {} seconds", pdsProductTimeoutInSeconds);
        return pdsPrepareProcessTimeoutSeconds;
    }
}
