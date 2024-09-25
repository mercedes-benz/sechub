-- SPDX-License-Identifier: MIT
CREATE TABLE adm_project
(
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   project_description varchar(512), -- description fields always 512 chars
   version integer,
   project_owner varchar(60),
   PRIMARY KEY (project_id)
);
CREATE TABLE adm_project_whitelist_uri
(
   project_project_id varchar(60) not null,  -- we accept 60 (3x20), see ProjectIdValidation
   project_whitelist_uris varchar(255) not null,
   PRIMARY KEY (project_project_id, project_whitelist_uris)
);
CREATE TABLE adm_user
(
   user_id varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   user_email_adress varchar(255) not null,
   user_apitoken varchar(255) not null,
   user_onetimetoken varchar(255),
   user_ott_created timestamp,
   user_superadmin boolean,
   user_deactivated boolean,
   version integer,
   PRIMARY KEY (user_id)
);
CREATE TABLE adm_project_to_user
(
   projects_project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidations
   users_user_id varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   PRIMARY KEY (projects_project_id, users_user_id)
);
CREATE TABLE adm_user_selfregistration
(
   user_id varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   email_adress varchar(255) not null,
   version integer,
   PRIMARY KEY (user_id)
);
CREATE TABLE auth_user
(
   user_id varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   user_apitoken varchar(255),
   role_admin boolean,
   role_user boolean,
   role_owner boolean,
   version integer,
   PRIMARY KEY (user_id)
);
CREATE TABLE scan_product_result
(
   uuid uuid not null,
   product_id varchar(30) not null, -- enum value, max:30
   result text,
   sechub_job_uuid uuid not null,
   started timestamp,
   ended timestamp,
   version integer,
   PRIMARY KEY (uuid)
);
CREATE TABLE scan_report
(
   uuid uuid not null,
   config varchar(8192), -- we accept maximum of 8192 chars (8kb)
   result text,
   sechub_job_uuid uuid,
   traffic_light varchar(30),  -- enum value, max:30
   project_id varchar(60) not null,  -- we accept 60 (3x20), see ProjectIdValidation
   started timestamp,
   ended timestamp,
   version integer,
   PRIMARY KEY (uuid)
);
CREATE TABLE scan_access
(
   project_id varchar(60) not null,  -- we accept 60 (3x20), see ProjectIdValidation
   user_id varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   version integer,
   PRIMARY KEY (project_id, user_id)
);
CREATE TABLE schedule_access
(
   project_id varchar(60) not null,  -- we accept 60 (3x20), see ProjectIdValidation
   user_id varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   version integer,
   PRIMARY KEY (project_id, user_id)
);
CREATE TABLE schedule_project_whitelist
(
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   uri varchar(255) not null,
   version integer,
   PRIMARY KEY (project_id, uri)
);
CREATE TABLE schedule_sechub_job
(
   uuid uuid not null,
   created timestamp not null,
   ended timestamp,
   result varchar(30) not null, -- enum value, max:30
   state varchar(30) not null, -- enum value, max:30
   configuration varchar(8192) not null, -- we accept maximum of 8192 chars (8kb)
   owner varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   started timestamp,
   traffic_light varchar(30),  -- enum value, max:30
   version integer,
   PRIMARY KEY (uuid)
);
CREATE TABLE scan_project_log
(
   uuid uuid not null,
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   executed_by varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   sechub_job_uuid uuid not null,
   config varchar(8192) not null, -- we accept maximum of 8192 chars (8kb)
   status varchar(30),
   started timestamp not null,
   ended timestamp,
   version integer,
   PRIMARY KEY (uuid)
)
;
CREATE TABLE adm_job_information
(
   uuid uuid not null,
   job_uuid uuid not null,
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   owner varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   status varchar(30) not null, -- enum value, max:30
   since timestamp not null,
   info varchar(512),  -- description fields always 512 chars
   configuration varchar(8912), -- we accept maximum of 8192 chars (8kb)
   version integer,
   PRIMARY KEY (uuid)
);
-- +++++++++++++++++++++++++++
--  Constraints ADM
-- +++++++++++++++++++++++++++
ALTER TABLE adm_user ADD CONSTRAINT c01_adm_user_emailadress unique (user_email_adress);
ALTER TABLE adm_project_to_user ADD CONSTRAINT c02_adm_project2user_user_id FOREIGN KEY (users_user_id) REFERENCES adm_user (user_id);
ALTER TABLE adm_project_to_user ADD CONSTRAINT c03_adm_project2user_project_id FOREIGN KEY (projects_project_id) REFERENCES adm_project (project_id);
ALTER TABLE adm_project_whitelist_uri ADD CONSTRAINT c04_adm_projectwhitelist_project_id FOREIGN KEY (project_project_id) REFERENCES adm_project (project_id);
ALTER TABLE adm_job_information ADD CONSTRAINT c05_adm_job_information_jobuuid unique (job_uuid);
ALTER TABLE adm_project ADD CONSTRAINT c06_adm_project2owner FOREIGN KEY (project_owner) REFERENCES adm_user (user_id);
