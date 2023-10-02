// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.whitelist;

import java.net.URI;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

import jakarta.transaction.Transactional;

@Service
public class ProjectWhiteListUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectWhiteListUpdateService.class);

    @Autowired
    ProjectWhitelistEntryRepository repository;

    @Autowired
    UserInputAssertion assertion;

    @Transactional
    public void update(String projectId, Set<URI> whitelist) {
        assertion.assertIsValidProjectId(projectId);

        LOG.info("remove old whitelist entries for project {}", projectId);
        /* we just remove all entries and recreate */
        repository.deleteAllEntriesForProject(projectId);

        for (URI uri : whitelist) {
            repository.save(new ProjectWhitelistEntry(projectId, uri));
        }
        LOG.info("updated project '{}' whitelist entries to: {}", projectId, whitelist);

    }

}
