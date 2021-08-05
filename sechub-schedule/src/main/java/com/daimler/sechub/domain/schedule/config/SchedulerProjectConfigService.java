// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.SecHubEnvironment;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;

@Service
public class SchedulerProjectConfigService {

	@Autowired
	SchedulerProjectConfigRepository repository;

	@Autowired
	SecHubEnvironment environmentData;

	@Autowired
	@Lazy
	DomainMessageService domainMessageService;

	public void setProjectAccessLevel(String projectId, ProjectAccessLevel level) {
	    SchedulerProjectConfig config = getOrCreateConfig(projectId);
	    config.setProjectAccessLevel(level);
	    
	    repository.save(config);
	}

	public ProjectAccessLevel getProjectAccessLevel(String projectId) {
		return getOrCreateConfig(projectId).getProjectAccessLevel();
	}

    private SchedulerProjectConfig getOrCreateConfig(String projectId) {
		Optional<SchedulerProjectConfig> config = repository.findById(projectId);
		if (config.isPresent()) {
			return config.get();
		}
		SchedulerProjectConfig newConfig = new SchedulerProjectConfig();
		return repository.save(newConfig);
	}

}
