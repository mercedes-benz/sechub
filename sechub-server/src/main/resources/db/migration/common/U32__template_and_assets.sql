-- SPDX-License-Identifier: MIT
ALTER TABLE IF EXISTS adm_project_templates DROP CONSTRAINT c09_adm_project_template_project_id;
DROP TABLE IF EXISTS adm_project_templates

DROP INDEX IF EXISTS i02_scan_template_id;
DROP TABLE IF EXISTS scan_template;

