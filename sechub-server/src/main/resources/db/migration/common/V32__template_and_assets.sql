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