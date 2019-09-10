package com.daimler.sechub.domain.schedule.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.sharedkernel.messaging.DomainMessageFactory;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;

@Service
public class ScheduleConfigService {

	@Autowired
	ScheduleConfigRepository repository;

	@Autowired
	DomainMessageService domainMessageService;


	public void setJobProcessingEnabled(boolean enabled) {
		ScheduleConfig config = getOrCreateConfig();
		if (enabled == config.isJobProcessingEnabled()) {
			return;
		}
		config.setJobProcessingEnabled(enabled);

		repository.save(config);

		domainMessageService.sendAsynchron(DomainMessageFactory.createRequestSchedulerStatusUpdateMessage());


	}

	private ScheduleConfig getOrCreateConfig() {
		Optional<ScheduleConfig> config = repository.findById(Integer.valueOf(0));
		if (config.isPresent()) {
			return config.get();
		}
		ScheduleConfig newConfig = new ScheduleConfig();
		return newConfig;
	}
}
