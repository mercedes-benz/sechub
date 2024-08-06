-- SPDX-License-Identifier: MIT
-- add column "messages" to schedule job table
ALTER TABLE schedule_sechub_job
   ADD COLUMN messages text