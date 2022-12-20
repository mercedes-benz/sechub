package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import java.io.IOException;
import java.util.List;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperExitCode;
import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

public class OwaspZapProductMessageHelper {

    private PDSUserMessageSupport productMessageSupport;

    public OwaspZapProductMessageHelper(String userMessagesFolder) {
        productMessageSupport = new PDSUserMessageSupport(userMessagesFolder, new TextFileWriter());
    }

    public void writeProductMessages(List<SecHubMessage> productMessages) throws IOException {
        productMessageSupport.writeMessages(productMessages);
    }

    /**
     * Write a SecHub message of type ERROR. The message content is derived from the
     * exit code of the ZapWrapperRuntimeException.
     *
     * @param zapWrapperRuntimeException
     * @throws IOException
     */
    public void writeProductError(ZapWrapperRuntimeException zapWrapperRuntimeException) throws IOException {
        ZapWrapperExitCode exitCode = zapWrapperRuntimeException.getExitCode();
        if (exitCode == null) {
            return;
        }

        switch (exitCode) {
        case TARGET_URL_NOT_REACHABLE:
            productMessageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR, "Target url specified inside sechub config json was not reachable."));
            break;
        case API_DEFINITION_CONFIG_INVALID:
            productMessageSupport.writeMessage(
                    new SecHubMessage(SecHubMessageType.ERROR, "Please use a single file for API definitions inside the filesystem->files part."));
            break;
        case TARGET_URL_INVALID:
            productMessageSupport
                    .writeMessage(new SecHubMessage(SecHubMessageType.ERROR, "Target url specified inside sechub config json was not a valid URL."));
            break;
        case PRODUCT_EXECUTION_ERROR:
            productMessageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR, "The DAST scanner OWASP ZAP ended because of a product error."));
            break;
        // Other possible errors are caused by internal misconfigurations, which means
        // issues users cannot change/influence
        default:
            break;
        }
    }

}
