-- SPDX-License-Identifier: MIT
-- remove former added column "messages" 
ALTER TABLE schedule_sechub_job
   DROP COLUMN messages text;