// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.support.JSONAdapterSupport;
import com.daimler.sechub.integrationtest.internal.TestJSONHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class AssertProject extends AbstractAssert {

    private TestProject project;

    AssertProject(TestProject project) {
        this.project = project;
    }

    /**
     * Assert the project does not exist in administration domain
     * 
     * @return
     */
    public AssertProject doesNotExist() {
        expectHttpClientError(HttpStatus.NOT_FOUND, () -> fetchProjectDetails(), project.getProjectId() + " found!");
        return this;
    }

    public AssertProject doesExist() {
        fetchProjectDetails();// will fail with http error when not available
        return this;

    }

    /**
     * Asserts the project has expected scan domain access entries
     * 
     * @param expected
     * @return count
     */
    public AssertProject hasAccessEntriesInDomainScan(long expected) {
        String projectId = project.getProjectId();
        long value = getRestHelper().getLongFromURL(getUrlBuilder().buildCountProjectScanAccess(projectId));

        assertEquals("Scan Access amount for project " + projectId + " is not as expected!", value, expected);

        return this;

    }

    /**
     * Asserts the project has expected schedule domain access entries
     * 
     * @param expected
     * @return count
     */
    public AssertProject hasAccessEntriesInDomainSchedule(long expected) {
        String projectId = project.getProjectId();
        long value = getRestHelper().getLongFromURL(getUrlBuilder().buildCountProjectScanAccess(projectId));

        assertEquals("Scan Access amount for project " + projectId + " is not as expected!", value, expected);

        return this;

    }

    public AssertProject hasProductResultsInDomainScan(int expected) {
        String projectId = project.getProjectId();
        long value = getRestHelper().getLongFromURL(getUrlBuilder().buildCountProjectProductResults(projectId));

        if (value != expected) {
            String json = getRestHelper().getJSon(getUrlBuilder().buildFetchAllProjectProductResultsButShrinked(projectId,200));
            fail("Expected product results for project " + projectId + "were : "+expected+" but resulted in: "+ value+"\n\nProduct Results:"+TestJSONHelper.get().beatuifyJSON(json));
        }
        

        return this;
    }

    public AssertProject hasScanReportsInDomainScan(int expected) {
        String projectId = project.getProjectId();
        long value = getRestHelper().getLongFromURL(getUrlBuilder().buildCountProjectScanReports(projectId));

        assertEquals("Product results amount for project " + projectId + " is not as expected!", value, expected);

        return this;
    }

    private String fetchProjectDetails() {
        return getRestHelper().getJSon(getUrlBuilder().buildAdminGetProjectDetailsUrl(project.getProjectId()));
    }

    public AssertProject hasOwner(TestUser user) {
        return hasOwner(user, true);
    }

    private AssertProject hasOwner(TestUser user, boolean expected) {
        String content = fetchProjectDetails();
        String owner = "<undefined>";
        try {
            owner = JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.fetch("owner", content).asText();
        } catch (AdapterException e) {
            e.printStackTrace();
            fail("adapter json failure:" + e.getMessage());
        }
        if (expected && !user.getUserId().equals(owner)) {
            fail("User:" + user.getUserId() + " is NOT owner of project:" + project.getProjectId() + " but:" + owner);
        } else if (!expected && user.getUserId().equals(owner)) {
            fail("User:" + user.getUserId() + " is owner of project:" + project.getProjectId());
        }
        return this;
    }

    public AssertProject hasNotOwner(TestUser user) {
        return hasOwner(user, false);
    }

    public AssertProject hasNoWhiteListEntries() {
        return hasWhiteListEntries();

    }

    public AssertProject hasWhiteListEntries(String... expectedArray) {

        String content = fetchProjectDetails();

        List<String> found = new ArrayList<>();
        ArrayNode whiteListArray;
        try {
            whiteListArray = JSONAdapterSupport.FOR_UNKNOWN_ADAPTER.fetch("whiteList", content).asArray();
            for (JsonNode node : whiteListArray) {
                found.add(node.asText());
            }
        } catch (AdapterException e) {
            e.printStackTrace();
            fail("adapter json failure:" + e.getMessage());
        }
        assertArrayEquals(expectedArray, found.toArray(new String[found.size()]));
        return this;
    }

}
