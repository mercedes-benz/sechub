// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import static org.mockito.Mockito.*;

import org.junit.Test;

import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

public class ScheduleDeleteAllProjectAcessServiceTest {

	@Test
	public void triggers_repository_delete_method_for_complete_project() {
		/* prepare */
		ScheduleDeleteAllProjectAcessService service = new  ScheduleDeleteAllProjectAcessService();
		service.repository=mock(ScheduleAccessRepository.class);
		service.assertion=mock(UserInputAssertion.class);

		/* execute */
		service.deleteAnyAccessDataForProject("project1");

		/* test */
		verify(service.repository).deleteAnyAccessForProject("project1");
	}

}
