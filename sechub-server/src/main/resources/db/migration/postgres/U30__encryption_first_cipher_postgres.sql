-- SPDX-License-Identifier: MIT

-- Remark: the scripts U31-U29 are only to provide the downgrade per SQL
-- but it IS NOT recommended to do this with encrypted data inside! This will
-- only work if all jobs are encrypted with NoneCipher, otherwise this means configuration data
-- loss because it will not contain valid json


-- convert to var char - we expect unencrypted_configuration is only "encrypted" with 'NONE'
update schedule_sechub_job ssj set
   configuration = convert_from(encrypted_configuration, 'UTF8')
