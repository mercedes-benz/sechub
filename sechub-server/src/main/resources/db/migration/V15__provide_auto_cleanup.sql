-- SPDX-License-Identifier: MIT
-- provide auto cleanup parts
CREATE TABLE admin_config -- the table contains only ONE row (like in scheduler_config)
(
   config_id integer not null,
   config_auto_cleanup  varchar(8192), -- we accept maximum of 8192 chars (8kb)
   config_auto_cleanup_in_days integer default 0, -- contains calculated value from message
   version integer,
   PRIMARY KEY (config_id)
);

ALTER TABLE schedule_config
   ADD COLUMN config_auto_cleanup_in_days integer default 0;
   

CREATE TABLE scan_config -- the table contains only ONE row (like in scheduler_config)
(
   config_id integer not null,
   config_auto_cleanup_in_days integer default 0, -- contains calculated value from message
   version integer,
   PRIMARY KEY (config_id)
);
 