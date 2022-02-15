-- SPDX-License-Identifier: MIT
CREATE TABLE scan_project_config
(
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   config_id varchar(60) not null, -- we accept 60 (3 x 20) see ScanProjectConfigID
   data varchar(4096) not null, -- we accept maximum of 4096 chars (4kb)
   version integer,
   PRIMARY KEY (project_id,config_id)
);
