-- SPDX-License-Identifier: MIT

-- we switch user_id size back to old 60 (3x20)
-- If this is not possible because already added bigger data, database shall give us an error, 
-- so we know we have to migrate those data manually...

ALTER TABLE adm_user ALTER COLUMN user_id TYPE varchar(60);
ALTER TABLE adm_user_selfregistration ALTER COLUMN user_id TYPE varchar(60);
ALTER TABLE auth_user ALTER COLUMN user_id TYPE varchar(60);
ALTER TABLE scan_access ALTER COLUMN user_id TYPE varchar(60);
ALTER TABLE schedule_access ALTER COLUMN user_id TYPE varchar(60);

ALTER TABLE adm_project_to_user ALTER COLUMN users_user_id TYPE varchar(60);

ALTER TABLE adm_project ALTER COLUMN project_owner TYPE varchar(60);

ALTER TABLE schedule_sechub_job ALTER COLUMN owner TYPE varchar(60);
ALTER TABLE adm_job_information ALTER COLUMN owner TYPE varchar(60);

ALTER TABLE scan_project_log ALTER COLUMN executed_by TYPE varchar(60);

-- we switch project_id size back to old 60 (3x20)
-- If this is not possible because already added bigger data, database shall give us an error, 
-- so we know we have to migrate those data manually...

ALTER TABLE adm_project ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE scan_report ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE scan_access ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE schedule_access ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE schedule_project_whitelist ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE schedule_sechub_job ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE scan_project_log ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE adm_job_information ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE scan_product_result ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE scan_project_config ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE adm_project_metadata ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE schedule_project_config ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE statistic_job ALTER COLUMN project_id TYPE varchar(60);
ALTER TABLE statistic_job_run ALTER COLUMN project_id TYPE varchar(60);

ALTER TABLE adm_project_whitelist_uri ALTER COLUMN project_project_id TYPE varchar(60);

ALTER TABLE adm_project_to_user ALTER COLUMN projects_project_id TYPE varchar(60);
ALTER TABLE scan_execution_profile_to_project ALTER COLUMN projects_project_id TYPE varchar(60);
