// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.mercedesbenz.sechub.sharedkernel.APIConstants;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

/**
 * Contains additional rest call functionality for integration tests on scan
 * domain
 *
 * @author Albert Tregnaghi
 *
 */
@RestController
@Profile(Profiles.INTEGRATIONTEST)
public class IntegrationTestScanRestController {

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
        return jobStatisticRepository.findById(sechubJobUUID);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/statistic/job-data/{sechubJobUUID}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<JobStatisticData> findJobStatisticData(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        return jobStatisticDataRepository.findAllBySechubJobUUID(sechubJobUUID);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/statistic/job-run/{sechubJobUUID}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public Optional<JobRunStatistic> findJobRunStatistic(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        return jobRunStatisticRepository.findById(sechubJobUUID);
    }

    @RequestMapping(path = APIConstants.API_ANONYMOUS + "integrationtest/statistic/job-run-data/{sechubJobUUID}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public List<JobRunStatisticData> findJobRunStatisticData(@PathVariable("sechubJobUUID") UUID sechubJobUUID) {
        return jobRunStatisticDataRepository.findAllByExecutionUUIDUUID(sechubJobUUID);
    }

}
