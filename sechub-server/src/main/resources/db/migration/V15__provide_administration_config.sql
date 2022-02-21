-- SPDX-License-Identifier: MIT
CREATE TABLE admin_config
(
   config_id integer not null,
   config_auto_cleanup  varchar(8192), -- we accept maximum of 8192 chars (8kb)
   version integer,
   PRIMARY KEY (config_id)
);