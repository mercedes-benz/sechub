-- SPDX-License-Identifier: MIT
-- project access level database parts

-- admin domain
ALTER TABLE adm_project 
   DROP COLUMN project_access_level
;

-- scheduler 
DROP TABLE schedule_project_config;