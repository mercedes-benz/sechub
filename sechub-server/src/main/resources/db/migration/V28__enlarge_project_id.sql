-- SPDX-License-Identifier: MIT

-- Update project id : we now accept 255 characters see ProjectIdValidation
ALTER TABLE adm_project ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE scan_report ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE scan_access ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE schedule_access ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE schedule_project_whitelist ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE schedule_sechub_job ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE scan_project_log ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE adm_job_information ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE scan_product_result ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE scan_project_config ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE adm_project_metadata ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE schedule_project_config ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE statistic_job ALTER COLUMN project_id TYPE varchar(255);
ALTER TABLE statistic_job_run ALTER COLUMN project_id TYPE varchar(255);

ALTER TABLE adm_project_whitelist_uri ALTER COLUMN project_project_id TYPE varchar(255);

ALTER TABLE adm_project_to_user ALTER COLUMN projects_project_id TYPE varchar(255);
ALTER TABLE scan_execution_profile_to_project ALTER COLUMN projects_project_id TYPE varchar(255);