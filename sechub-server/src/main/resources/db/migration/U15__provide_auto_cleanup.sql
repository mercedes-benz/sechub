-- SPDX-License-Identifier: MIT
-- drop auto cleanup parts
DROP TABLE IF EXISTS admin_config;

ALTER TABLE schedule_config
   DROP COLUMN config_auto_cleanup_in_days;

CREATE TABLE IF EXISTS scan_config;