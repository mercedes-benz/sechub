-- SPDX-License-Identifier: MIT

-- Update user id : we now accept 120 (3 x 40) see UserIdValidation

ALTER TABLE adm_user ALTER COLUMN user_id TYPE varchar(120);  
ALTER TABLE adm_project_to_user ALTER COLUMN users_user_id TYPE varchar(120);  
ALTER TABLE adm_user_selfregistration ALTER COLUMN user_id TYPE varchar(120);  

ALTER TABLE auth_user ALTER COLUMN user_id TYPE varchar(120);  
ALTER TABLE scan_access ALTER COLUMN user_id TYPE varchar(120);  
ALTER TABLE schedule_access ALTER COLUMN user_id TYPE varchar(120);  

ALTER TABLE adm_project ALTER COLUMN project_owner TYPE varchar(120);  

ALTER TABLE schedule_sechub_job ALTER COLUMN owner TYPE varchar(120);  
ALTER TABLE adm_job_information ALTER COLUMN owner TYPE varchar(120);  

ALTER TABLE scan_project_log ALTER COLUMN executed_by TYPE varchar(120);  