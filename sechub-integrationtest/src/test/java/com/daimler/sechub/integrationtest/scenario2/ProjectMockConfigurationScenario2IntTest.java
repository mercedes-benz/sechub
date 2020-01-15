// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario2;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static com.daimler.sechub.integrationtest.scenario2.Scenario2.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;
import com.daimler.sechub.sharedkernel.type.TrafficLight;

public class ProjectMockConfigurationScenario2IntTest {

	@Rule
	public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario2.class);
	private String mockDataCodeScanYellowLowerCased = "{\"codeScan\" : { \"result\" : \"yellow\"}}";
	private String mockDataEmpty = "{}";
	
	/* ------------------------------------------------*/
	/* -----------------SET----------------------------*/
	/* ------------------------------------------------*/
	
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

	/* ------------------------------------------------*/
	/* -----------------GET----------------------------*/
	/* ------------------------------------------------*/
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
		assertEquals(expect("codeScan",TrafficLight.YELLOW), as(SUPER_ADMIN).getProjectMockConfiguration(PROJECT_1));
	}
	
	@Test
	public void user_with_access_can_get_project_mockconfiguration() {
		/* prepare */
		as(SUPER_ADMIN).setProjectMockConfiguration(PROJECT_1, mockDataCodeScanYellowLowerCased);
		as(SUPER_ADMIN).assignUserToProject(USER_1, PROJECT_1);

		/* execute + test */
		assertEquals(expect("codeScan",TrafficLight.YELLOW), as(USER_1).getProjectMockConfiguration(PROJECT_1));
	}
	
	@Test
	public void user_without_access_cannot_get_project_mockconfiguration() {
		/* prepare */
		as(SUPER_ADMIN).setProjectMockConfiguration(PROJECT_1, mockDataCodeScanYellowLowerCased);

		/* execute + test */
		expectHttpFailure(()-> as(USER_1).getProjectMockConfiguration(PROJECT_1),HttpStatus.NOT_FOUND);
	}
	
	private static String expect(String scanType, TrafficLight trafficLight) {
		return "{\""+scanType+"\":{\"result\":\""+trafficLight.name()+"\"}}";
	}

}
