// SPDX-License-Identifier: MIT
package com.daimler.sechub.server;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.schedule.access.ScheduleAccess;
import com.daimler.sechub.domain.schedule.access.ScheduleAccess.ProjectAccessCompositeKey;
import com.daimler.sechub.domain.schedule.access.ScheduleAccessRepository;
import com.daimler.sechub.domain.schedule.whitelist.ProjectWhitelistEntry;
import com.daimler.sechub.domain.schedule.whitelist.ProjectWhitelistEntry.ProjectWhiteListEntryCompositeKey;
import com.daimler.sechub.domain.schedule.whitelist.ProjectWhitelistEntryRepository;
import com.daimler.sechub.sharedkernel.Profiles;

@Component
@Profile(Profiles.DEMOMODE)
public class DemoModeTestDataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(DemoModeTestDataInitializer.class);

	@Bean
	public CommandLineRunner createDemoAccess(ScheduleAccessRepository repository) {
		return args -> {
			// only as long as we do not have a real user and project management...
			String projectId = "testproject";
			createAccess(repository, projectId, "alice");
			createAccess(repository, projectId, "developer");
		};
	}
	
	@Bean
	public CommandLineRunner createDemoWhitelisting(ProjectWhitelistEntryRepository repository) {
		return args -> {
			// only as long as we do not have a real user and project management...
			String projectId = "testproject";
			createWhiteListEntry(repository, projectId, URI.create("https://vulnerable.demo.example.org"));
			createWhiteListEntry(repository, projectId, URI.create("https://safe.demo.example.org"));
		};
	}

	private void createWhiteListEntry(ProjectWhitelistEntryRepository repository, String projectId, URI uri) {
	
		ProjectWhiteListEntryCompositeKey key = new ProjectWhiteListEntryCompositeKey(projectId, uri);
		repository.save( new ProjectWhitelistEntry(key));
	}

	private void createAccess(ScheduleAccessRepository repository, String projectId, String userId) {
		ProjectAccessCompositeKey key = new ProjectAccessCompositeKey(userId, projectId);
		Optional<ScheduleAccess> project = repository.findById(key);
		if (project.isPresent()) {
			// setup done
			return;
		}
		repository.save(new ScheduleAccess(key));
		LOG.info("No access found for {} to {} so created!",userId,projectId);
	}

	
}
