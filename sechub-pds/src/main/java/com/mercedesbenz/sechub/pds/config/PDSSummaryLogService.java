package com.mercedesbenz.sechub.pds.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class PDSSummaryLogService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSSummaryLogService.class);

    @Autowired
    PDSServerConfigurationService configurationService;

    @EventListener(ApplicationReadyEvent.class)
    void applicationReady() {
        PDSServerConfiguration configuration = configurationService.getServerConfiguration();

        StringBuilder summary = new StringBuilder();
        summary.append("PDS has been started successfully.\n**************************\n        Summary\n**************************");
        summary.append("\n- config file used: ").append(configurationService.pathToConfigFile);

        summary.append("\n- server id: ").append(configuration.getServerId());
        summary.append("\n- system wide minutes to wait for product: ").append(configurationService.getMinutesToWaitForProduct());
        summary.append("\n- minimum configurable minutes to wait for product: ").append(configurationService.getMinimumConfigurableMinutesToWaitForProduct());
        summary.append("\n- maximum configurable minutes to wait for product: ").append(configurationService.getMaximumConfigurableMinutesToWaitForProduct());

        List<PDSProductSetup> products = configuration.getProducts();
        summary.append("\n- Available products: ").append(products.size());
        for (PDSProductSetup setup : products) {
            summary.append("\n  * ").append(setup.getId());
        }

        LOG.info(summary.toString());

    }
}
