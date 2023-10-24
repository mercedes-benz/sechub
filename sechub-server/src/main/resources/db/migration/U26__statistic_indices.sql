-- SPDX-License-Identifier: MIT

-- indices on statistics tables to enhance reporting performance

DROP INDEX IF EXISTS i01_statistic_job_run_project;

DROP INDEX IF EXISTS i01_statistic_job_run_data_filter;
