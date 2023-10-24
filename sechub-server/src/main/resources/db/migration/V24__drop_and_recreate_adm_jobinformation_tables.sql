-- SPDX-License-Identifier: MIT
DROP TABLE IF EXISTS adm_job_information;
CREATE TABLE adm_job_information
(
   job_uuid uuid not null,
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   owner varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   status varchar(30) not null, -- enum value, max:30
   since timestamp not null,
   info varchar(512),  -- description fields always 512 chars
   configuration varchar(8912), -- we accept maximum of 8192 chars (8kb)
   version integer,
   PRIMARY KEY (job_uuid)
);