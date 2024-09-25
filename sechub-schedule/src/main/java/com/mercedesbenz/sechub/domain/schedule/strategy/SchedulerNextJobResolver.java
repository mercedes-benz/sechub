package com.mercedesbenz.sechub.domain.schedule.strategy;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.DocumentationScopeConstants;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class SchedulerNextJobResolver {

    private static final long DEFAULT_MIN_SUSPED_DURATION = 1000;

    @Autowired
    ScheduleEncryptionService encryptionService;

    @Autowired
    public SecHubJobRepository jobRepository;

    @Autowired
    SchedulerStrategyProvider schedulerStrategyProvider;

    @Value("${sechub.scheduler.nextjob.suspend.miniumumduration.milliseconds:" + DEFAULT_MIN_SUSPED_DURATION + "}")
    @MustBeDocumented(scope = DocumentationScopeConstants.SCOPE_SCHEDULE, value = """
            The scheduler automatically restarts the next suspended jobs, regardless of the defined schedule strategy.
            This is done to get suspended jobs of another shut down instance back up and running as quickly as possible.

            To avoid suspended jobs being restarted too quickly, you can use this value to set the minimum time that must pass
            before the next suspended job can be restarted. The value is defined in milliseconds.

            The (previous) end date of the suspended job is used. For example, this value is important for
            K8s redeployment, because the servers that have not yet been updated should not immediately continue with the
            suspended jobs - they will also be shut down soon and would suspend the restarted jobs again...
            """)
    long minimumSuspendDurationInMilliseconds;

    /**
     * Resolves next job by given strategy. But before strategy is used, suspended
     * jobs are fetched - if they are not too young (to avoid race conditions when
     * we we have an ongoing deployment).
     *
     * @param strategy strategy to use when there are no suspended jobs
     * @return uuid of job or <code>null</code> if there is no job to execute
     */
    public UUID resolveNextJob() {

        SchedulerStrategy schedulerStrategy = schedulerStrategyProvider.build();

        Set<Long> supportedPoolIds = encryptionService.getCurrentEncryptionPoolIds();

        if (supportedPoolIds == null || supportedPoolIds.isEmpty()) {
            return null;
        }
        /*
         * we always fetch the next possible suspended job - no matter which kind of
         * strategy
         */
        Optional<UUID> nextSuspendedJob = jobRepository.nextJobIdToExecuteSuspended(supportedPoolIds, minimumSuspendDurationInMilliseconds);
        if (nextSuspendedJob.isPresent()) {
            return nextSuspendedJob.get();
        }

        /* No suspended jobs available - use strategy to find next one */
        Optional<UUID> nextJob = schedulerStrategy.nextJobId(supportedPoolIds);
        if (!nextJob.isPresent()) {
            return null;
        }

        return nextJob.get();
    }

}
