-- SPDX-License-Identifier: MIT

-- we switch user_id size back to old 60 (3x20)
-- If this is not possible because already added bigger data, database shall give us an error, 
-- so we know we have to migrate those data manually...

ALTER TABLE adm_user ALTER COLUMN user_id TYPE varchar(60);  
ALTER TABLE adm_project_to_user ALTER COLUMN users_user_id TYPE varchar(60);  
ALTER TABLE adm_user_selfregistration ALTER COLUMN user_id TYPE varchar(60);  

ALTER TABLE auth_user ALTER COLUMN user_id TYPE varchar(60);  
ALTER TABLE scan_access ALTER COLUMN user_id TYPE varchar(60);  
ALTER TABLE schedule_access ALTER COLUMN user_id TYPE varchar(60);  

ALTER TABLE adm_project ALTER COLUMN project_owner TYPE varchar(60);  

ALTER TABLE schedule_sechub_job ALTER COLUMN owner TYPE varchar(60);  
ALTER TABLE adm_job_information ALTER COLUMN owner TYPE varchar(60);  

ALTER TABLE scan_project_log ALTER COLUMN executed_by TYPE varchar(60);  