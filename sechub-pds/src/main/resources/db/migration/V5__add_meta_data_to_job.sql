-- SPDX-License-Identifier: MIT
-- add column "meta_data" to job table
ALTER TABLE pds_job
   ADD COLUMN meta_data text