-- SPDX-License-Identifier: MIT
CREATE TABLE adm_project_metadata
(
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   metadata_key varchar(60) not null,
   metadata_value varchar(255), --not null,
   version integer,
   PRIMARY KEY (project_id, metadata_key)
);
ALTER TABLE adm_project_metadata ADD CONSTRAINT c07_adm_projectmetadata_project_id FOREIGN KEY (project_id) REFERENCES adm_project (project_id);