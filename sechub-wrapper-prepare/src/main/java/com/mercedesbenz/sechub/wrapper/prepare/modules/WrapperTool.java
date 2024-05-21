package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

@Component
public abstract class WrapperTool {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperTool.class);
    private static final int defaultMinutesToWaitForProduct = PDSDefaultParameterValueConstants.DEFAULT_MINUTES_TO_WAIT_FOR_PRODUCT;

    @Value("${" + PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES + ":" + defaultMinutesToWaitForProduct + "}")
    private int pdsProductTimeoutMinutes;

    @Value("${" + KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS + ":-1}")
    private int pdsPrepareProcessTimeoutSeconds;

    @Autowired
    public PDSProcessAdapterFactory processAdapterFactory;

    protected abstract void cleanUploadDirectory(String uploadDirectory) throws IOException;

    protected void waitForProcessToFinish(ProcessAdapter process) {

        LOG.debug("Wait for wrapper to finish process.");
        int seconds = calculateTimeoutSeconds();

        boolean exitDoneInTime = false;
        try {
            exitDoneInTime = process.waitFor(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Wrapper for executed modul " + this.getClass().getSimpleName() + " could not finish process.", e);
        }

        if (!exitDoneInTime) {
            throw new RuntimeException("Wrapper for executed modul " + this.getClass().getSimpleName() + " could not finish process. Waited "
                    + pdsPrepareProcessTimeoutSeconds + " seconds.");
        }

        if (process.exitValue() != 0) {
            throw new RuntimeException(
                    "Wrapper for executed modul " + this.getClass().getSimpleName() + " process failed with exit code: " + process.exitValue());
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
