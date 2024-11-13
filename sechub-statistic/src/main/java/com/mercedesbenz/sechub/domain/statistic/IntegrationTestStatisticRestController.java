// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mercedesbenz.sechub.domain.statistic.job.JobRunStatistic;
import com.mercedesbenz.sechub.domain.statistic.job.JobRunStatisticData;
import com.mercedesbenz.sechub.domain.statistic.job.JobRunStatisticDataRepository;
import com.mercedesbenz.sechub.domain.statistic.job.JobRunStatisticRepository;
import com.mercedesbenz.sechub.domain.statistic.job.JobStatistic;
import com.mercedesbenz.sechub.domain.statistic.job.JobStatisticData;
import com.mercedesbenz.sechub.domain.statistic.job.JobStatisticDataRepository;
import com.mercedesbenz.sechub.domain.statistic.job.JobStatisticRepository;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.APIConstants;

/**
 * Contains additional rest call functionality for integration tests on scan
 * domain
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestStatisticRestController {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestStatisticRestController.class);

    @Autowired
    private JobStatisticRepository jobStatisticRepository;

    @Autowired
    private JobStatisticDataRepository jobStatisticDataRepository;

    @Autowired
    private JobRunStatisticRepository jobRunStatisticRepository;

    @Autowired
    private JobRunStatisticDataRepository jobRunStatisticDataRepository;

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/statistic/job/{sechubJobUUID}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public Optional<JobStatistic> findJobStatistic(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        Optional<JobStatistic> result = jobStatisticRepository.findById(sechubJobUUID);
        LOG.debug("Fetched all job statistic entities for sechub job: {}, found: {}", sechubJobUUID, result.isPresent());
        return result;
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/statistic/job-data/{sechubJobUUID}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<JobStatisticData> findJobStatisticData(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        List<JobStatisticData> result = jobStatisticDataRepository.findAllBySechubJobUUID(sechubJobUUID);
        LOG.debug("Fetched all job statistic data entities for sechub job: {}, found: {}", sechubJobUUID, result.size());
        return result;
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/statistic/job-run/{sechubJobUUID}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<JobRunStatistic> findAllJobRunStatisticsForSecHubJob(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        List<JobRunStatistic> result = jobRunStatisticRepository.findAllBySechubJobUUID(sechubJobUUID);
        LOG.debug("Fetched all job run statistic entities for sechub job: {}, found: {}", sechubJobUUID, result.size());
        return result;
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/statistic/job-run-data/{executionUUID}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<JobRunStatisticData> findAllJobRunStatisticDataForExecutionUUID(@PathVariable("executionUUID") UUID executionUUID) {
        List<JobRunStatisticData> result = jobRunStatisticDataRepository.findAllByExecutionUUID(executionUUID);
        LOG.debug("Fetched all job run statistic data entities for execution uuid: {}, found: {}", executionUUID, result.size());
        return result;
    }

}
