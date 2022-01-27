-- SPDX-License-Identifier: MIT
-- project access level database parts

-- admin domain
ALTER TABLE adm_project 
   ADD COLUMN project_access_level varchar(30) -- we accept 30 (3x10), see ProjectAccessLevel
          DEFAULT 'full' -- set default, see ProjectAccessLevel#FULL_ACCESS
;

-- scheduler 
CREATE TABLE schedule_project_config
(
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   project_access_level varchar(30) not null, -- we accept 30 (3x10), see ProjectAccessLevel
   
   version integer,
   PRIMARY KEY (project_id)
);


-- scan domain: not necessary, we reuse existing ScanProjectConfig entities