-- SPDX-License-Identifier: MIT
-- add column "messages" to job table
ALTER TABLE pds_job
   ADD COLUMN messages text