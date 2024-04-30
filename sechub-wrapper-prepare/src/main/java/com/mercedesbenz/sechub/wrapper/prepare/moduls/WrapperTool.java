package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SealedObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

@Component
abstract class WrapperTool {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperTool.class);
    private static final int defaultMinutesToWaitForProduct = PDSDefaultParameterValueConstants.DEFAULT_MINUTES_TO_WAIT_FOR_PRODUCT;

    @Value("${" + PARAM_KEY_PDS_CONFIG_PRODUCT_TIMEOUT_MINUTES + ":" + defaultMinutesToWaitForProduct + "}")
    private int pdsProductTimeoutMinutes;

    @Value("${" + KEY_PDS_PREPARE_PROCESS_TIMEOUT_SECONDS + ":-1}")
    private int pdsPrepareProcessTimeoutSeconds;

    ProcessAdapter process;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    @Autowired
    UserInputEscaper userInputEscaper;

    abstract void cleanUploadDirectory(String uploadDirectory) throws IOException;

    void escapeRepositoryURL(String repositoryURL, List<String> forbiddenCharacters) {
        userInputEscaper.escapeLocationURL(repositoryURL, forbiddenCharacters);
    }

    void waitForProcessToFinish() {

        LOG.debug("Wait for wrapper to finish process.");
        int seconds = privateCalculateTimeoutSeconds();

        boolean exitDoneInTime = false;
        try {
            exitDoneInTime = process.waitFor(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("GIT wrapper could not finish process.", e);
        }

        if (!exitDoneInTime) {
            throw new RuntimeException("GIT wrapper could not finish process. Waited " + pdsPrepareProcessTimeoutSeconds + " minutes.");
        }
    }

    void exportEnvironmentVariables(ProcessBuilder builder, Map<String, SealedObject> credentialMap) throws IOException {
        Map<String, String> environment = builder.environment();
        if (credentialMap != null && !credentialMap.isEmpty()) {
            for (Map.Entry<String, SealedObject> entry : credentialMap.entrySet()) {
                try {
                    environment.put(entry.getKey(), CryptoAccess.CRYPTO_STRING.unseal(entry.getValue()));
                } catch (Exception e) {
                    throw new IOException("Error while unsealing credential: " + entry.getKey(), e);
                }
            }
        }
    }

    private int privateCalculateTimeoutSeconds() {
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
