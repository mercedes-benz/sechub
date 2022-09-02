// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;

@Service
public class CheckmarxWrapperStorageService {

    @Autowired
    CheckmarxWrapperEnvironment environment;

    @Autowired
    TextFileWriter writer;

    @Autowired
    PDSUserMessageSupport messageSupport;

    public void store(AdapterExecutionResult adapterResult) throws IOException {

        writeProductMessages(adapterResult);
        writeProductResult(adapterResult);

    }

    private void writeProductResult(AdapterExecutionResult adapterResult) throws IOException {
        String pdsResultFilePath = environment.getPdsResultFile();
        if (pdsResultFilePath == null) {
            throw new IllegalStateException("PDS result file not set!");
        }

        File pdsResultFile = new File(pdsResultFilePath);
        writer.save(pdsResultFile, adapterResult.getProductResult(), true);
    }

    private void writeProductMessages(AdapterExecutionResult adapterResult) throws IOException {
        String pdsUserMessagesFolder = environment.getPdsUserMessagesFolder();
        if (pdsUserMessagesFolder == null) {
            throw new IllegalStateException("PDS user messages folder is not set!");
        }
        messageSupport.writeMessages(adapterResult.getProductMessages());
    }
}
