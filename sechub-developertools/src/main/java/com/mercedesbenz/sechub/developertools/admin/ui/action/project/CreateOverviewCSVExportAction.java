// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action.project;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedesbenz.sechub.developertools.admin.export.Grid;
import com.mercedesbenz.sechub.developertools.admin.export.Row;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.mercedesbenz.sechub.integrationtest.internal.TestJSONHelper;

/**
 * This action will create a simple overview Grid as a CVS file
 *
 * @author Albert Tregnaghi
 *
 */
public class CreateOverviewCSVExportAction extends AbstractUIAction {
    private static final long serialVersionUID = 1L;

    public CreateOverviewCSVExportAction(UIContext context) {
        super("Create overview CSV export", context);
    }

    @Override
    public void execute(ActionEvent e) {

        Row title = Row.builder().add("Project").add("Owner").add("Users").add("whitelists").add("information").build();
        Grid grid = new Grid(title);

        List<String> projectIds = loadProjectIds();
        List<String> allUsers = loadUserIds();
        List<String> signupUsers = loadSignupUserIds();

        int projectsDone = 0;
        for (String projectId : projectIds) {
            outputAsTextOnSuccess("Loading project :" + projectId + " ->" + projectsDone + "/" + projectIds.size());
            String data = getContext().getAdministration().fetchProjectInfo(projectId);
            TestJSONHelper jsonHelper = TestJSONHelper.get();

            JsonNode tree = jsonHelper.readTree(data);
            JsonNode usersNode = tree.get("users");
            JsonNode ownerNode = tree.get("owner");
            JsonNode whiteListsNode = tree.get("whiteList");

            StringBuilder userListSb = createCommaSeparatedListOfEntries(usersNode, allUsers);
            StringBuilder whiteListSb = createCommaSeparatedListOfEntries(whiteListsNode);

            Row row = Row.builder().add(projectId).add(ownerNode.asText()).add(userListSb.toString()).add(whiteListSb.toString()).add("").build();

            grid.add(row);

            projectsDone++;
        }

        /* add unrelated users in pseudo - project */
        Row row1 = Row.builder().add("<NO PROJECT>").add("<NO OWNER>").add(allUsers.toString()).add("")
                .add("Inside SecHub registered, but not assigned to any project!").build();
        grid.add(row1);

        /* add signup users in pseudo - project */
        for (String signupUser : signupUsers) {
            Row row2 = Row.builder().add("<WAITING SIGNUP>").add("<NO OWNER>").add(signupUser).add("").add("Waiting for signup").build();
            grid.add(row2);
        }

        try {

            Path tempFile = Files.createTempFile("sechub_devtools_adminui_export", ".csv");
            try (BufferedWriter buffer = Files.newBufferedWriter(tempFile)) {
                buffer.write(grid.toCSVString());
                outputAsTextOnSuccess("written CSV data to " + tempFile.toAbsolutePath().toFile().getAbsolutePath());
            }
        } catch (IOException e2) {
            outputAsTextOnSuccess("FAILED");
            e2.printStackTrace();

        }

    }

    private List<String> loadUserIds() {
        String data = getContext().getAdministration().fetchUserList();
        TestJSONHelper jsonHelper = TestJSONHelper.get();
        JsonNode tree = jsonHelper.readTree(data);

        List<String> ids = new ArrayList<>();

        Iterator<JsonNode> iditerator = tree.elements();
        while (iditerator.hasNext()) {
            JsonNode dNode = iditerator.next();
            ids.add(dNode.asText());
        }
        outputAsTextOnSuccess("Found " + ids.size() + " users in system.");
        return ids;
    }

    private List<String> loadSignupUserIds() {
        String data = getContext().getAdministration().fetchSignups();
        TestJSONHelper jsonHelper = TestJSONHelper.get();
        JsonNode tree = jsonHelper.readTree(data);

        List<String> ids = new ArrayList<>();

        Iterator<JsonNode> iditerator = tree.elements();
        while (iditerator.hasNext()) {
            JsonNode dNode = iditerator.next();
            String userId = dNode.get("userId").asText();
            String emailAddress = dNode.get("emailAddress").asText();
            ids.add(userId + " <" + emailAddress + ">");
        }
        outputAsTextOnSuccess("Found " + ids.size() + " waiting signups.");
        return ids;
    }

    private StringBuilder createCommaSeparatedListOfEntries(JsonNode idListNode) {
        return createCommaSeparatedListOfEntries(idListNode, null);
    }

    private StringBuilder createCommaSeparatedListOfEntries(JsonNode idListNode, List<String> removeMe) {
        StringBuilder idListSb = new StringBuilder();
        Iterator<JsonNode> iditerator = idListNode.elements();
        while (iditerator.hasNext()) {
            JsonNode idNode = iditerator.next();
            String identifier = idNode.asText();
            if (removeMe != null) {
                removeMe.remove(identifier);
            }
            idListSb.append(identifier);
            if (iditerator.hasNext()) {
                idListSb.append(", ");
            }
        }
        return idListSb;
    }

    private List<String> loadProjectIds() {
        String data = getContext().getAdministration().fetchProjectList();
        TestJSONHelper jsonHelper = TestJSONHelper.get();
        JsonNode tree = jsonHelper.readTree(data);

        List<String> projectIds = new ArrayList<>();

        Iterator<JsonNode> projectIditerator = tree.elements();
        while (projectIditerator.hasNext()) {
            JsonNode projectIdNode = projectIditerator.next();
            projectIds.add(projectIdNode.asText());
        }
        outputAsTextOnSuccess("Found " + projectIds.size() + " projects in system.");
        return projectIds;
    }

}