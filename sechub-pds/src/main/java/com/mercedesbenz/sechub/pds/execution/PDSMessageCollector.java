// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.pds.util.PDSAssert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

public class PDSMessageCollector {

    private static final Logger LOG = LoggerFactory.getLogger(PDSMessageCollector.class);

    TextFileReader reader = new TextFileReader();

    public List<SecHubMessage> collect(File folder) {
        notNull(folder, "Folder must be not null!");

        List<SecHubMessage> messages = new ArrayList<>();

        for (File file : folder.listFiles()) {
            try {
                String text = reader.readTextFromFile(file);

                SecHubMessageType type = SecHubMessageType.INFO;
                String fileName = file.getName().toUpperCase();
                if (fileName.startsWith("WARNING_")) {
                    type = SecHubMessageType.WARNING;
                } else if (fileName.startsWith("ERROR_")) {
                    type = SecHubMessageType.ERROR;
                }

                SecHubMessage message = new SecHubMessage(type, text);
                messages.add(message);

            } catch (IOException e) {
                LOG.error("Was not able to read message file: {}", file, e);
            }
        }

        return messages;
    }
}
