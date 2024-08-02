-- SPDX-License-Identifier: MIT

-- New encryption columns.
-- We allow null values here explicit. This happens only for old unencrypted data 
-- and will result in restarts  of current running unencrypted jobs (if necessary at all)
-- by SecHub
ALTER TABLE pds_job ADD COLUMN encrypted_configuration bytea;
ALTER TABLE pds_job ADD COLUMN encrypt_initial_vector bytea;
ALTER TABLE pds_job ADD COLUMN encryption_out_of_synch boolean;

-- Delete old configuration column (+data), existing data will be lost - this is a wanted behavor
-- see comment before.
ALTER TABLE pds_job DROP COLUMN configuration; 
