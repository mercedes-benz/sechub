// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperScanService;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperStorageService;

@Component
public class CheckmarxWrapperCLI implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperCLI.class);

    @Autowired
    CheckmarxWrapperScanService scanService;

    @Autowired
    CheckmarxWrapperStorageService storageService;

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Checkmarx wrapper starting");

        try {
            AdapterExecutionResult result = scanService.startScan();
            storageService.store(result);

        } catch (Exception e) {
            LOG.error("Execution failed", e);

            System.exit(2);
        }

    }

}
