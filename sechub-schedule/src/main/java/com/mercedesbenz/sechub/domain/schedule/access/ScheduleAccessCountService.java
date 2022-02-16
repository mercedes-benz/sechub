// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScheduleAccessCountService {

    @Autowired
    ScheduleAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    public long countProjectAccess(String projectId) {
        assertion.isValidProjectId(projectId);

        ScheduleAccess probe = new ScheduleAccess();
        probe.key = new ProjectAccessCompositeKey(null, projectId);
        Example<ScheduleAccess> example = Example.of(probe);

        return repository.count(example);

    }

}
