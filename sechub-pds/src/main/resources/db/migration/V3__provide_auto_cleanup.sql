-- SPDX-License-Identifier: MIT
-- provide auto cleanup parts
CREATE TABLE pds_config -- the table contains only ONE row
(
   config_id integer not null,
   config_auto_cleanup  varchar(8192), -- we accept maximum of 8192 chars (8kb)
   config_auto_cleanup_in_days integer default 2, -- contains calculated value
   version integer,
   PRIMARY KEY (config_id)
);
