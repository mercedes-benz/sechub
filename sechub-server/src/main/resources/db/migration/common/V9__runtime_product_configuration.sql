-- SPDX-License-Identifier: MIT
CREATE TABLE scan_product_executor_config
(
   config_uuid uuid not null,
   config_name varchar(90) not null, -- we accept 90 (3 x 30) see ProductExecutorConfigValidation 
   config_executor_version integer,
   config_product_id varchar(30) not null, -- enum value, max:30
   
   config_setup text,
   config_enabled boolean,

   version integer,
   
   PRIMARY KEY (config_uuid)
)
;

CREATE TABLE scan_product_execution_profile
(
   profile_id varchar(90) not null, -- we accept 90 (3x30), see ProfileIdIdValidation
   profile_description varchar(512), -- description fields always 512 chars
   profile_enabled boolean,
   
   version integer,
   
   PRIMARY KEY (profile_id)
)
;

CREATE TABLE scan_execution_profile_to_config
(
   profiles_profile_id varchar(60) not null, -- we accept 60 (3x20), see ProfileIdValidations
   configurations_config_uuid uuid,
   PRIMARY KEY (profiles_profile_id, configurations_config_uuid)
);

-- we do not reference projects, but only store project ids. reason: other domain (we are in scan
-- domain and not admin domain, so only know projectId but no project entity 
CREATE TABLE scan_execution_profile_to_project
(
   product_execution_profile_profile_id varchar(60) not null, -- we accept 60 (3x20), see ProfileIdValidations
   projects_project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidations
   
   PRIMARY KEY (product_execution_profile_profile_id, projects_project_id)
);

-- +++++++++++++++++++++++++++
--  Constraints SCAN
-- +++++++++++++++++++++++++++
ALTER TABLE scan_execution_profile_to_config ADD CONSTRAINT c07_scan_profile_to_config_config_uuid FOREIGN KEY (configurations_config_uuid) REFERENCES scan_product_executor_config (config_uuid);
ALTER TABLE scan_execution_profile_to_config ADD CONSTRAINT c08_scan_profile_to_config_profileid FOREIGN KEY (profiles_profile_id) REFERENCES scan_product_execution_profile (profile_id);