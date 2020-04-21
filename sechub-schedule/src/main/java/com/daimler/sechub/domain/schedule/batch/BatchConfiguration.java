// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.daimler.sechub.domain.schedule.job.SecHubJobRepository;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;

@Configuration
@EnableBatchProcessing
// see
// https://docs.spring.io/spring-batch/trunk/reference/html/configureJob.html
public class BatchConfiguration {

	public static final String JOB_NAME_EXECUTE_SCAN = "executeScan";

    @Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public JobRepository jobRepository;

	@Autowired
	private DomainMessageService eventBusService;

	@Autowired
	private SecHubJobRepository secHubJobRepository;

	@Autowired
	private SecHubJobSafeUpdater secHubJobUpdater;

	@Bean
	public AsyncJobLauncher createJobLauncher() {
		AsyncJobLauncher launcher = new AsyncJobLauncher();
		launcher.setJobRepository(jobRepository);
		return launcher;
	}

	@Bean
	public Job executeScan() {

		BatchJobExecutionScope scope = new BatchJobExecutionScope();

		/* @formatter:off */

    	return jobBuilderFactory.get(JOB_NAME_EXECUTE_SCAN).
    			incrementer(new RunIdIncrementer()).
                listener(scope).
                repository(jobRepository).
                flow(step1Execute(scope)).
                end().
          build();
                
        /* @formatter:on */
	}

	/* +-----------------------------------------------------------------------+ */
	/* +............................ STEPS ....................................+ */
	/* +-----------------------------------------------------------------------+ */
	@Bean
	public Step step1Execute(BatchJobExecutionScope scope) {
		/* @formatter:off */
        return stepBuilderFactory.get("step1Execute").
        		allowStartIfComplete(true).
        		tasklet(new ScanExecutionTasklet(scope)).
                build();
        /* @formatter:on */
	}

	class BatchJobExecutionScope implements JobExecutionListener {

		private JobExecution jobExecution;

		public JobExecution getJobExecution() {
			return jobExecution;
		}

		@Override
		public void beforeJob(JobExecution jobExecution) {
			this.jobExecution = jobExecution;
		}

		@Override
		public void afterJob(JobExecution jobExecution) {
		}

		public DomainMessageService getEventBusService() {
			return eventBusService;
		}

		public SecHubJobRepository getSecHubJobRepository() {
			return secHubJobRepository;
		}

		public SecHubJobSafeUpdater getSecHubJobUpdater() {
			return secHubJobUpdater;
		}

	}
}