// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.status;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mercedesbenz.sechub.developertools.JSONDeveloperHelper;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;

public class ListStatusEntriesAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;
    private String lastData;

    public ListStatusEntriesAction(UIContext context) {
        super("List status entries", context);
    }

    @Override
    public void execute(ActionEvent e) {
        lastData = getContext().getAdministration().getStatusList();
        outputAsBeautifiedJSONOnSuccess(lastData);
    }

    public String getLastData() {
        return lastData;
    }

    public Map<String, String> getLastDataAsKeyValueMap() {
        String json = getLastData();
        if (json == null) {
            return Collections.emptyMap();
        }
        JSONDeveloperHelper h = new JSONDeveloperHelper();
        TreeMap<String, String> map = new TreeMap<>();
        JsonNode root;
        try {
            root = h.getMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("json not readable", e);
        }
        ArrayNode array = (ArrayNode) root;
        for (JsonNode node : array) {
            JsonNode key = node.get("key");
            JsonNode value = node.get("value");

            String keyText = key.asText();
            String valueText = value.asText();
            map.put(keyText, valueText);
        }
        return map;
    }
}