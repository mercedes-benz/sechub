// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

public class ScheduleSecHubJobSpecifications {

    public static Specification<ScheduleSecHubJob> hasProjectIdAndData(String projectId, Map<String, String> wantedDataEntries) {
        return (job, query, criteriaBuilder) -> {

            /* @formatter:off
             *
             * SQL pseudo example - so easier to understand what is wanted/going on:
             *
             *    select * from schedule_sechub_job j where j.projectId="projec1" and where exists (
             *                  select 1 from ScheduleSecHubJobData d where
             *                             d.jobUUID=j.jobUUID
             *                             d.id="metadata.labels.stage"
             *                                and
             *                             d.val="testing"
             *
             *              )
             *          and where exists (
             *                  select 1 from schedule_sechub_job_data d where
             *                             d.jobUUID=j.jobUUID
             *                             d.id="metadata.labels.purpose"
             *                                and
             *                             d.val="security"
             *
             *
             * @formatter:on */
            job.alias("job");
            List<Predicate> andPredicates = new ArrayList<>();
            andPredicates.add(criteriaBuilder.equal(job.<String>get(ScheduleSecHubJob.PROPERTY_PROJECT_ID), projectId));

            if (wantedDataEntries != null && !wantedDataEntries.isEmpty()) {

                for (Map.Entry<String, String> entry : wantedDataEntries.entrySet()) {
                    String labelId = entry.getKey();
                    String labelValue = entry.getValue();

                    Predicate whereDataExists = createWhereDataExistsForIdAndValueLike(job, query, criteriaBuilder, labelId, labelValue);
                    andPredicates.add(whereDataExists);

                }
            }

            return criteriaBuilder.and(andPredicates.toArray(new Predicate[andPredicates.size()]));

        };
    }

    private static Predicate createWhereDataExistsForIdAndValueLike(Root<ScheduleSecHubJob> job, CriteriaQuery<?> query, CriteriaBuilder cb, String labelId,
            String labelValue) {

        Subquery<Integer> subQuery = query.subquery(Integer.class);
        Root<ScheduleSecHubJobData> data = subQuery.from(ScheduleSecHubJobData.class);
        data.alias("data");

        /* @formatter:off */
        subQuery.
          select(cb.literal(1)).
          where(
              cb.and(
                    cb.equal(data.get(ScheduleSecHubJobData.PROPERTY_JOB_UUID), job.get(ScheduleSecHubJob.PROPERTY_UUID)),
                    cb.equal(data.get(ScheduleSecHubJobData.PROPERTY_ID), labelId),
                    cb.equal(data.get(ScheduleSecHubJobData.PROPERTY_VALUE), labelValue)
              )
          );
        /* @formatter:on */

        Predicate whereDataExists = cb.exists(subQuery);
        return whereDataExists;
    }
}
