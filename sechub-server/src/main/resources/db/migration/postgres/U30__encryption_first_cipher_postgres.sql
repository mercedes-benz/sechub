-- SPDX-License-Identifier: MIT

-- convert to var char - we expect unencrypted_configuration is only "encrypted" with 'NONE'
update schedule_sechub_job ssj set
   configuration = convert_from(encrypted_configuration, 'UTF8')
   
DELETE FROM schedule_cipher_pool_data;
