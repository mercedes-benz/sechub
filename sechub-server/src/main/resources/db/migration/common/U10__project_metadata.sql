-- SPDX-License-Identifier: MIT
-- drop constraints
ALTER TABLE adm_project_metadata DROP CONSTRAINT c07_adm_projectmetadata_project_id;
-- drop tables
DROP TABLE IF EXISTS adm_project_metadata CASCADE;