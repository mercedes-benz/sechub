-- SPDX-License-Identifier: MIT
-- drop constraints
ALTER TABLE adm_user DROP CONSTRAINT c01_adm_user_emailadress;
ALTER TABLE adm_project_to_user DROP CONSTRAINT c02_adm_project2user_user_id;
ALTER TABLE adm_project_to_user DROP CONSTRAINT c03_adm_project2user_project_id;
ALTER TABLE adm_project_whitelist_uri DROP CONSTRAINT c04_adm_projectwhitelist_project_id;
ALTER TABLE adm_job_information DROP CONSTRAINT c05_adm_job_information_jobuuid;
ALTER TABLE adm_project DROP CONSTRAINT c06_adm_project2owner;
-- drop tables
DROP TABLE IF EXISTS adm_project_to_owner CASCADE;
DROP TABLE IF EXISTS adm_project CASCADE;
DROP TABLE IF EXISTS adm_project_to_user CASCADE;
DROP TABLE IF EXISTS adm_project_whitelist_uri CASCADE;
DROP TABLE IF EXISTS adm_user CASCADE;
DROP TABLE IF EXISTS adm_user_to_roles CASCADE;
DROP TABLE IF EXISTS adm_user_selfregistration CASCADE;
DROP TABLE IF EXISTS auth_user CASCADE;
DROP TABLE IF EXISTS scan_product_result CASCADE;
DROP TABLE IF EXISTS scan_report CASCADE;
DROP TABLE IF EXISTS schedule_access CASCADE;
DROP TABLE IF EXISTS schedule_project_whitelist CASCADE;
DROP TABLE IF EXISTS schedule_sechub_job CASCADE;
DROP TABLE IF EXISTS adm_job_information CASCADE;
DROP TABLE IF EXISTS scan_access CASCADE;
DROP TABLE IF EXISTS scan_project_log CASCADE;
