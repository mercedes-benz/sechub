package com.daimler.sechub.domain.administration.schedule;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.administration.status.StatusEntry;
import com.daimler.sechub.domain.administration.status.StatusEntryRepository;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.IsReceivingAsyncMessage;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.SchedulerMessage;

@Component
public class SchedulerMessageHandler implements AsynchronMessageHandler{

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerMessageHandler.class);

	@Autowired
	StatusEntryRepository repository;

	@Override
	public void receiveAsyncMessage(DomainMessage request) {
		MessageID messageId = request.getMessageId();
		LOG.debug("received domain request: {}", request);

		switch (messageId) {
		case SCHEDULER_STATUS_UPDATE :
			handleSchedulerStatusChange(request);
			break;
		default:
			throw new IllegalStateException("unhandled message id:"+messageId);
		}
	}

	@IsReceivingAsyncMessage(MessageID.SCHEDULER_STATUS_UPDATE)
	private void handleSchedulerStatusChange(DomainMessage request) {
		SchedulerMessage status = request.get(MessageDataKeys.SCHEDULER_STATUS_DATA);

		StatusEntry enabled = fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_ENABLED);
		enabled.setValue(Boolean.toString(status.isEnabled()));

		StatusEntry jobsAll= fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_ALL);
		jobsAll.setValue(Integer.toString(status.getAmountOfAllJobs()));

		StatusEntry jobsRunning = fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_RUNNING);
		jobsRunning.setValue(Integer.toString(status.getAmountOfRunningJobs()));

		StatusEntry jobsWaiting = fetchOrCreateEntry(SchedulerStatusEntryKeys.SCHEDULER_JOBS_WAITING);
		jobsWaiting.setValue(Integer.toString(status.getAmountOfWaitingJobs()));

		/* persist */
		repository.save(enabled);
		repository.save(jobsAll);
		repository.save(jobsRunning);
		repository.save(jobsWaiting);

	}

	private StatusEntry fetchOrCreateEntry(SchedulerStatusEntryKeys key) {
		Optional<StatusEntry> optional = repository.findByStatusEntryKey(key);
		if (optional.isPresent()) {
			return optional.get();
		}
		StatusEntry entry = new StatusEntry(key);
		return entry;

	}
}
