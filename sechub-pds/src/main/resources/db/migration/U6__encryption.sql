-- SPDX-License-Identifier: MIT
ALTER TABLE pds_job DROP COLUMN encrypted_configuration bytea;
ALTER TABLE pds_job DROP COLUMN encrypt_initial_vector bytea;
ALTER TABLE pds_job DROP COLUMN encryption_out_of_sync;

ALTER TABLE pds_job ADD COLUMN configuration varchar(8192) not null; 
