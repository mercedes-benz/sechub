package com.daimler.sechub.domain.schedule.access;

import static org.mockito.Mockito.*;
import org.junit.Test;

public class ScheduleUserAccessToProjectValidationServiceTest {
	
	@Test
	public void test() {
		/* prepare */
		ScheduleUserAccessToProjectValidationService service = mock(ScheduleUserAccessToProjectValidationService.class);
		service.accessRepository = mock(ScheduleAccessRepository.class);
		
		/* execute */
		service.assertUserHasAccessToProject("project1");
		
		/* test */
		verify(service).assertUserHasAccessToProject("project1");
	}
}
