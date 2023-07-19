-- SPDX-License-Identifier: MIT
DROP TABLE IF EXISTS batch_step_execution_context;
DROP TABLE IF EXISTS batch_step_execution;

DROP TABLE IF EXISTS batch_job_execution_params;
DROP TABLE IF EXISTS batch_job_execution_context;
DROP TABLE IF EXISTS batch_job_execution;
DROP TABLE IF EXISTS batch_job_instance;

DROP SEQUENCE IF EXISTS batch_job_execution_seq;
DROP SEQUENCE IF EXISTS batch_job_seq;
DROP SEQUENCE IF EXISTS batch_step_execution_seq;
