// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.domain.scan.access.ScanAccess.ProjectAccessCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@Service
public class ScanAccessCountService {

    @Autowired
    ScanAccessRepository repository;

    @Autowired
    UserInputAssertion assertion;

    public long countProjectAccess(String projectId) {
        assertion.isValidProjectId(projectId);

        ScanAccess probe = new ScanAccess();
        probe.key = new ProjectAccessCompositeKey(null, projectId);
        Example<ScanAccess> example = Example.of(probe);

        return repository.count(example);

    }

}
