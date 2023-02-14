-- SPDX-License-Identifier: MIT
ALTER TABLE schedule_sechub_job
    
   ADD COLUMN scan_group varchar(20);  -- enum value, max:20