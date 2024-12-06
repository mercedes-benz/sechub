-- SPDX-License-Identifier: MIT
CREATE TABLE scan_template
(
   template_id varchar(40) not null, -- we accept 60 (3x20), see ProjectIdValidation
   template_definition text not null, -- we accept 60 (3 x 20) see ScanProjectConfigID
   version integer,
   
   PRIMARY KEY (template_id)
);

CREATE INDEX IF NOT EXISTS i02_scan_template_id
    ON scan_template (template_id);
    
CREATE TABLE adm_project_templates
(
    project_project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
    project_template_id varchar(40) not null,
    PRIMARY KEY (project_project_id, project_template_id)
);

ALTER TABLE adm_project_templates ADD CONSTRAINT c09_adm_project_template_project_id FOREIGN KEY (project_project_id) REFERENCES adm_project (project_id);

CREATE TABLE scan_asset_file
(
     asset_id varchar(40) not null, -- 40 characters allowed, see AssetIdValidation
     file_name varchar(100) not null, -- 100 characters allowed, see AssetFileNameValidation
     data bytea, -- not null,
     checksum varchar(80) not null,
     version integer,
     
     PRIMARY KEY (asset_id, file_name)
);