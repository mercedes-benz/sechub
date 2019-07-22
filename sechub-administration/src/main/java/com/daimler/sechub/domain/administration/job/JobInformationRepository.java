// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JobInformationRepository extends JpaRepository<JobInformation, UUID> {

	public Optional<JobInformation> findByJobUUID(UUID jobUUID);

	@Query(JobInformation.QUERY_FIND_ALL_RUNNING_JOBS)
	public List<JobInformation> findAllRunningJobs();

	@Transactional
	@Modifying
	@Query(JobInformation.QUERY_DELETE_JOBINFORMATION_FOR_JOBUUID)
	public void deleteJobInformationWithJobUUID(@Param("jobUUID") UUID jobUUID);
}
