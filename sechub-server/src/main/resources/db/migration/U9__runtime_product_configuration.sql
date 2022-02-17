-- SPDX-License-Identifier: MIT
-- drop constraints
ALTER TABLE scan_execution_profile_to_config DROP CONSTRAINT c07_scan_profile_to_config_config_uuid;
ALTER TABLE scan_execution_profile_to_config DROP CONSTRAINT c08_scan_profile_to_config_profileid;

-- drop tables
DROP TABLE IF EXISTS scan_product_executor_config CASCADE;
DROP TABLE IF EXISTS scan_product_execution_profile CASCADE;
DROP TABLE IF EXISTS scan_execution_profile_to_config CASCADE;
DROP TABLE IF EXISTS scan_execution_profile_to_project CASCADE;

