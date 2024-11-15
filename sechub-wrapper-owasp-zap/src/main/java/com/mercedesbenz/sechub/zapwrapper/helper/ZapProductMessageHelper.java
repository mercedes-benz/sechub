// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

public class ZapProductMessageHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ZapProductMessageHelper.class);

    private final PDSUserMessageSupport productMessageSupport;

    public ZapProductMessageHelper(String userMessagesFolder) {
        productMessageSupport = new PDSUserMessageSupport(userMessagesFolder, new TextFileWriter());
    }

    public void writeProductMessages(List<SecHubMessage> productMessages) {
        try {
            productMessageSupport.writeMessages(productMessages);
        } catch (IOException e) {
            LOG.error("Could not write user Message because {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    public void writeSingleProductMessage(SecHubMessage productMessage) {
        try {
            productMessageSupport.writeMessage(productMessage);
        } catch (IOException e) {
            LOG.error("Could not write user Message because {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Write a SecHub message of type ERROR. The message content is derived from the
     * exit code of the ZapWrapperRuntimeException.
     *
     * @param zapWrapperRuntimeException
     * @throws IOException
     */
    public void writeProductError(ZapWrapperRuntimeException zapWrapperRuntimeException) {
        ZapWrapperExitCode exitCode = zapWrapperRuntimeException.getExitCode();
        try {
            writeProductErrorForExitCode(exitCode);
        } catch (IOException e) {
            LOG.error("Could not write user Message because {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private void writeProductErrorForExitCode(ZapWrapperExitCode exitCode) throws IOException {
        if (exitCode == null) {
            return;
        }

        switch (exitCode) {
        case TARGET_URL_NOT_REACHABLE:
            productMessageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR,
                    "Target URL not reachable. Please check if the target URL, specified inside SecHub configuration, is reachable."));
            break;
        case API_DEFINITION_CONFIG_INVALID:
            productMessageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR,
                    "Please check your webscan api section inside the sechub configuration file. Only use supported api types and use filesystem->files instead of filesystem->folders for the API definition files."));
            break;
        case TARGET_URL_INVALID:
            productMessageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR,
                    "Target URL invalid. The target URL, specified inside SecHub configuration, is not a valid URL."));
            break;
        case PRODUCT_EXECUTION_ERROR:
            productMessageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR, "Product error. The DAST scanner ZAP ended with a product error."));
            break;
        case INVALID_INCLUDE_OR_EXCLUDE_URLS:
            productMessageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR,
                    "The web scan could not be executed because some includes or excludes could not be transformed into valid URLs realtive to the target URL. Please follow the documentation on how to specify includes or excludes for webscans."));
            break;
        // Other possible errors are caused by internal misconfigurations, which means
        // issues users cannot change/influence
        default:
            break;
        }
    }

}
