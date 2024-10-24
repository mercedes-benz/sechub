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
    project_templates varchar(40) not null,
    PRIMARY KEY (project_project_id, project_templates)
);

ALTER TABLE adm_project_templates ADD CONSTRAINT c09_adm_project_template_project_id FOREIGN KEY (project_project_id) REFERENCES adm_project (project_id);