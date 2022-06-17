// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario2;

import static com.mercedesbenz.sechub.integrationtest.api.AssertExecutionResult.*;
import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;
import static com.mercedesbenz.sechub.integrationtest.scenario2.Scenario2.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.api.TestProject;
import com.mercedesbenz.sechub.integrationtest.api.TestUser;
import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestExampleConstants;
import com.mercedesbenz.sechub.integrationtest.internal.MockData;

public class ProjectMockConfigurationScenario2IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);
    private String mockDataCodeScanYellowLowerCased = createMockScanConfig("codeScan", TrafficLight.YELLOW);

    private String mockDataEmpty = "{}";

    /* ------------------------------------------------ */
    /* -----------------SET---------------------------- */
    /* ------------------------------------------------ */

    @Test
    public void a_superadmin_can_set_project_mockconfiguration() {
        assertUser(SUPER_ADMIN).canSetMockConfiguration(PROJECT_1, mockDataEmpty);
    }

    @Test
    public void anonymous_cannot_set_project_mockconfiguration() {
        assertUser(ANONYMOUS).canNotSetMockConfiguration(PROJECT_1, mockDataEmpty, HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void user_without_access_cannot_set_project_mockconfiguration() {
        assertUser(USER_1).canNotSetMockConfiguration(PROJECT_1, mockDataEmpty, HttpStatus.NOT_FOUND);
    }

    /* ------------------------------------------------ */
    /* -----------------GET---------------------------- */
    /* ------------------------------------------------ */
    @Test
    public void a_superadmin_can_get_project_mockconfiguration_when_not_set__result_is_null() {
        assertEquals(null, as(SUPER_ADMIN).getProjectMockConfiguration(PROJECT_1));
    }

    @Test
    public void a_superadmin_can_get_project_mockconfiguration_when_set_result_is_as_set() {
        /* prepare */

        as(SUPER_ADMIN).setProjectMockConfiguration(PROJECT_1, mockDataCodeScanYellowLowerCased);
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* execute + test */
        assertEquals(expect("codeScan", TrafficLight.YELLOW), as(SUPER_ADMIN).getProjectMockConfiguration(PROJECT_1));
    }

    @Test
    public void user_with_access_can_get_project_mockconfiguration() {
        /* prepare */
        as(SUPER_ADMIN).setProjectMockConfiguration(PROJECT_1, mockDataCodeScanYellowLowerCased);
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* execute + test */
        assertEquals(expect("codeScan", TrafficLight.YELLOW), as(USER_1).getProjectMockConfiguration(PROJECT_1));
    }

    @Test
    public void user_without_access_cannot_get_project_mockconfiguration() {
        /* prepare */
        as(SUPER_ADMIN).setProjectMockConfiguration(PROJECT_1, mockDataCodeScanYellowLowerCased);

        /* execute + test */
        expectHttpFailure(() -> as(USER_1).getProjectMockConfiguration(PROJECT_1), HttpStatus.NOT_FOUND);
    }

    /* ------------------------------------------------ */
    /* -----------------Adapter behaviour ------------- */
    /* ------------------------------------------------ */
/* @formatter:off */
    @Test
    public void a_user_can_change_mock_configuration_and_behavior() {
        TestUser user = USER_1;
        TestProject project = PROJECT_1;

        /* Step1: prepare */
        as(SUPER_ADMIN).
           assignUserToProject(user, project).
           updateWhiteListForProject(project, Arrays.asList(IntegrationTestExampleConstants.INFRASCAN_DEFAULT_WHITELEIST_ENTRY, MockData.NETSPARKER_RED_ZERO_WAIT.getTarget()));

        /* test web scan yellow */
        as(user).setProjectMockConfiguration(project, createMockScanConfig("webScan", TrafficLight.YELLOW));
        assertResult(as(user).createWebScanAndFetchScanData(project)).isYellow();

        /* test web scan green */
        as(user).setProjectMockConfiguration(project, createMockScanConfig("webScan", TrafficLight.GREEN));
        assertResult(as(user).createWebScanAndFetchScanData(project)).isGreen();

        /* test code scan red */
        as(user).setProjectMockConfiguration(project, createMockScanConfig("codeScan", TrafficLight.RED));
        assertResult(as(user).withSecHubClient().startAndWaitForCodeScan(project)).isRed();

        /* test infra scan green */
        as(user).setProjectMockConfiguration(project, createMockScanConfig("infraScan", TrafficLight.GREEN));
        assertResult(as(user).withSecHubClient().createInfraScanAndFetchScanData(project)).isGreen();

    }
    /* @formatter:on */

    private static String expect(String scanType, TrafficLight trafficLight) {
        return "{\"" + scanType + "\":{\"result\":\"" + trafficLight.name() + "\"}}";
    }

    private static String createMockScanConfig(String scanType, TrafficLight trafficLight) {
        return "{\"" + scanType + "\" : { \"result\" : \"" + trafficLight.name().toLowerCase() + "\"}}";
    }
}
