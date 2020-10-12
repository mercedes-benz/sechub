// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.config;

import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class DeleteConfigurationAction extends AbstractUIAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DeleteConfigurationAction.class);

    public DeleteConfigurationAction(UIContext context) {
        super("Delete executor configuration", context);
    }

    @Override
    public void execute(ActionEvent e) {
        Optional<String> opt = getUserInput("Please enter uuid for config to DELETE", InputCacheIdentifier.EXECUTOR_CONFIG_UUID);
        if (!opt.isPresent()) {
            return;
        }
        String configUUIDAsString=opt.get().trim();
        if (!confirm("Do you really want to\nDELETE\nconfig " + configUUIDAsString + "?")) {
            outputAsTextOnSuccess("CANCELED - delete");
            LOG.info("canceled delete of config {}", configUUIDAsString);
            return;
        }
        LOG.info("start delete of config {}", configUUIDAsString);
        String infoMessage = getContext().getAdministration().deletExecutionConfig(UUID.fromString(configUUIDAsString));
        outputAsTextOnSuccess(infoMessage);
    }

}