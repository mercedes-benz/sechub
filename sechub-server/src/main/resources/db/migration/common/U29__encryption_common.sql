-- SPDX-License-Identifier: MIT
ALTER TABLE schedule_sechub_job DROP COLUMN encrypted_configuration bytea;
ALTER TABLE schedule_sechub_job DROP COLUMN encrypt_initial_vector bytea;
ALTER TABLE schedule_sechub_job DROP COLUMN encrypt_pool_data_id integer;

DROP TABLE IF EXISTS schedule_cipher_pool_data;

ALTER TABLE adm_job_information ADD COLUMN configuration varchar(8912);
ALTER TABLE scan_project_log ADD COLUMN config varchar(8912);
ALTER TABLE scan_report ADD COLUMN config varchar(8912); 
