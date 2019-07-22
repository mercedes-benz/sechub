// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * A special job launcher which uses a simple async task executor so job is
 * executed asynchronous (in spite of default implementation in
 * {@link SimpleJobLauncher} which is synchronous)
 * 
 * @author Albert Tregnaghi
 *
 */
public class AsyncJobLauncher extends SimpleJobLauncher {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncJobLauncher.class);

	public AsyncJobLauncher() {
		this.setTaskExecutor(new SimpleAsyncTaskExecutor("async-job-launcher"));
	}

	@Override
	public JobExecution run(Job job, JobParameters jobParameters) throws JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		LOG.info("async run of job :{}", job.getName());
		return super.run(job, jobParameters);
	}
}