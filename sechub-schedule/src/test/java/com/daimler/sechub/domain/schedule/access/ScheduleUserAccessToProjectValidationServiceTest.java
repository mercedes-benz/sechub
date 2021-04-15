// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class ScheduleUserAccessToProjectValidationServiceTest {
	
	@Test
	public void triggers_assert_user_has_access_to_project() {
		/* prepare */
		ScheduleUserAccessToProjectValidationService service = mock(ScheduleUserAccessToProjectValidationService.class);
		service.accessRepository = mock(ScheduleAccessRepository.class);
		
		/* execute */
		service.assertUserHasAccessToProject("project1");
		
		/* test */
		verify(service).assertUserHasAccessToProject("project1");
	}
}
