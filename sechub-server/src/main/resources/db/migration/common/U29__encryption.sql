-- SPDX-License-Identifier: MIT

-- Remark: the scripts U31-U29 are only to provide the downgrade per SQL
-- but it IS NOT recommended to do this with encrypted data inside! This will
-- only work if all jobs are encrypted with NoneCipher, otherwise this means configuration data
-- loss because it will not contain valid json

-- (U30 scripts will have copied encrypted conifg bytes as text to configuration) 
ALTER TABLE schedule_sechub_job DROP COLUMN encrypted_configuration;
ALTER TABLE schedule_sechub_job DROP COLUMN encrypt_initial_vector;
ALTER TABLE schedule_sechub_job DROP COLUMN encrypt_pool_data_id;

-- encryption parts
DROP TABLE schedule_cipher_pool_data;