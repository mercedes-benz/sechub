package com.mercedesbenz.sechub.commons.pds;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

public class PDSUserMessageSupport {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private File messageFolder;
    private TextFileWriter writer = new TextFileWriter();

    public PDSUserMessageSupport(String userMessageFolder) {
        messageFolder = new File(userMessageFolder);
    }

    public void writeMessages(List<SecHubMessage> productMessages) throws IOException {
        for (SecHubMessage message : productMessages) {
            writeMessage(message);
        }
    }

    public void writeMessage(SecHubMessage message) throws IOException {
        String fileName = createUniqueFileName(message);

        File file = new File(messageFolder, fileName);

        writer.save(file, message.getText(), false);
    }

    private String createUniqueFileName(SecHubMessage message) {
        StringBuilder sb = new StringBuilder();
        SecHubMessageType type = message.getType();
        if (type != null) {
            sb.append(type.name());
            sb.append("_");
        }
        sb.append("message_");
        sb.append(DATE_FORMAT.format(new Date()));
        sb.append("_");
        sb.append(System.nanoTime());
        sb.append(".txt");

        String fileName = sb.toString();
        return fileName;
    }

}
