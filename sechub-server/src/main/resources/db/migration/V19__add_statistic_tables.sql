-- SPDX-License-Identifier: MIT
-- add statistic tables
CREATE TABLE statistic_job
(
   sechub_job_uuid uuid not null,
   
   created timestamp not null,
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   
   version integer,
   PRIMARY KEY (sechub_job_uuid)
);

CREATE TABLE statistic_job_run
(
   uuid uuid not null,
   
   sechub_job_uuid uuid not null,
   started timestamp not null,
   ended timestamp not null,
   traffic_light varchar(30),  -- enum value, max:30
   
   version integer,
   PRIMARY KEY (uuid)
);

CREATE TABLE statistic_scan_execution
(
   execution_uuid uuid not null,
   
   sechub_job_uuid uuid not null,
   started timestamp not null,
   ended timestamp not null,
   failed boolean,
      
   version integer,
   PRIMARY KEY (execution_uuid)
);
