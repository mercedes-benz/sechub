-- SPDX-License-Identifier: MIT
CREATE TABLE adm_status
(
   status_id varchar(60) not null, -- we accept 60 (3x20),
   status_value varchar(512), -- description fields max 512 chars
   version integer,
   PRIMARY KEY (status_id)
);
CREATE TABLE schedule_config
(
   config_id integer not null,
   config_job_processing_enabled boolean not null,
   version integer,
   PRIMARY KEY (config_id)
);