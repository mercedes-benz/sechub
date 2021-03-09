// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.AssertExecutionResult.*;
import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;

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

    @Test
    public void user_with_access_can_set_project_mockconfiguration() {
        /* prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

        /* execute + test */
        assertUser(USER_1).canSetMockConfiguration(PROJECT_1, mockDataEmpty);
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

    @Test
    public void default_setup__as_user_of_project_configure_mocks_trigger_web_scan_and_expect_wished_results() {
        /* Step1: prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);
        as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("webScan", TrafficLight.YELLOW));

        /* execute + test */
        assertResult(as(USER_1).createWebScanAndFetchScanData(PROJECT_1)).isYellow();

        /* Step2: prepare */
        as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("webScan", TrafficLight.RED));

        /* execute + test */
        assertResult(as(USER_1).createWebScanAndFetchScanData(PROJECT_1)).isRed();

        /* Step2: prepare */
        as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("webScan", TrafficLight.GREEN));

        /* execute + test */
        assertResult(as(USER_1).createWebScanAndFetchScanData(PROJECT_1)).isGreen();

    }

    @Test
    public void default_setup__as_user_of_project_configure_mocks_trigger_code_scan_and_expect_wished_results() {
        /* Step1: prepare */
        as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);
        as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("codeScan", TrafficLight.YELLOW));

        /* execute + test */

        assertResult(as(USER_1).withSecHubClient().startAndWaitForCodeScan(PROJECT_1)).isYellow();

        /* Step2: prepare */
        as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("codeScan", TrafficLight.RED));

        /* execute + test */
        assertResult(as(USER_1).withSecHubClient().startAndWaitForCodeScan(PROJECT_1)).isRed();

        /* Step2: prepare */
        as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("codeScan", TrafficLight.GREEN));

        /* execute + test */
        assertResult(as(USER_1).withSecHubClient().startAndWaitForCodeScan(PROJECT_1)).isGreen();

    }

    /* @formatter:off */
	@Test
	public void default_setup__as_user_of_project_configure_mocks_trigger_infra_scan_and_expect_wished_results() {
		/* Step1: prepare */
		as(SUPER_ADMIN).
			assignUserToProject(USER_1, PROJECT_1).
			updateWhiteListForProject(PROJECT_1, Collections.singletonList("https://fscan.intranet.example.org/"));
		as(USER_1).
			setProjectMockConfiguration(PROJECT_1, createMockScanConfig("infraScan", TrafficLight.YELLOW));

		/* execute + test */
		assertResult(as(USER_1).withSecHubClient().createInfraScanAndFetchScanData(PROJECT_1)).isYellow();
	
		/* Step2: prepare */
		as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("infraScan", TrafficLight.RED));

		/* execute + test */
		assertResult(as(USER_1).withSecHubClient().createInfraScanAndFetchScanData(PROJECT_1)).isRed();
		
		/* Step2: prepare */
		as(USER_1).setProjectMockConfiguration(PROJECT_1, createMockScanConfig("infraScan", TrafficLight.GREEN));

		/* execute + test */
		assertResult(as(USER_1).withSecHubClient().createInfraScanAndFetchScanData(PROJECT_1)).isGreen();
	
	}
	/* @formatter:on */

    private static String expect(String scanType, TrafficLight trafficLight) {
        return "{\"" + scanType + "\":{\"result\":\"" + trafficLight.name() + "\"}}";
    }

    private static String createMockScanConfig(String scanType, TrafficLight trafficLight) {
        return "{\"" + scanType + "\" : { \"result\" : \"" + trafficLight.name().toLowerCase() + "\"}}";
    }
}
