-- SPDX-License-Identifier: MIT
-- we switch back to old 4096 char constraint
-- If this is not possible because already added bigger data, database shall give us an error, 
-- so we know we have to migrate those data manually...
ALTER TABLE scan_project_config ALTER COLUMN data varchar (4096) not null;
