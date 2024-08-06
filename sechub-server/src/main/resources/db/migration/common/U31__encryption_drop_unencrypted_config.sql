-- SPDX-License-Identifier: MIT

-- Remark: the scripts U31-U29 are only to provide the downgrade per SQL
-- but it IS NOT recommended to do this with encrypted data inside! This will
-- only work if all jobs are encrypted with NoneCipher, otherwise this means configuration data
-- loss because it will not contain valid json


-- revert table structure add old columns again

ALTER TABLE schedule_sechub_job ADD COLUMN configuration varchar(8192); 
-- Only one configuration persistence shall exist inside SecHub database:
ALTER TABLE adm_job_information ADD COLUMN configuration varchar(8192);
ALTER TABLE scan_project_log DROP COLUMN config varchar(8912); 
ALTER TABLE scan_report DROP COLUMN config varchar(8912);
