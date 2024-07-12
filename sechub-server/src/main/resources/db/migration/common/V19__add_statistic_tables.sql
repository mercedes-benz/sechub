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

CREATE TABLE statistic_job_data
(
   uuid uuid not null,
   sechub_job_uuid uuid not null,
   
   type varchar(90) not null, -- we accept 90 (3x30)
   id varchar(90) not null, -- we accept 90 (3x30), we use "id" ("key" is not possible because H2 keyword, so forbidden)
   val bigint not null, -- we must use "val" instead of "value" ("value" is not possible because H2 keyword, so forbidden)
   
   timestamp timestamp not null, -- timestamp when the data entry was initial created
   
   version integer,
   PRIMARY KEY (uuid)
);

CREATE TABLE statistic_job_run
(
   execution_uuid uuid not null,
   
   sechub_job_uuid uuid not null,
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   
   created timestamp not null,
   started timestamp,
   ended timestamp,
   
   failed boolean,
   traffic_light varchar(30),  -- enum value, max:30
   
   version integer,
   PRIMARY KEY (execution_uuid)
);

CREATE TABLE statistic_job_run_data
(
   uuid uuid not null,
   execution_uuid uuid not null,
   
   type varchar(90) not null, -- we accept 90 (3x30)
   id varchar(90) not null, -- we accept 90 (3x30), we use "id" ("key" is not possible because H2 keyword, so forbidden)
   val bigint not null, -- we must use "val" instead of "value" ("value" is not possible because H2 keyword, so forbidden)
   
   timestamp timestamp not null, -- timestamp when the data entry was initial created
   
   version integer,
   PRIMARY KEY (uuid)
);
