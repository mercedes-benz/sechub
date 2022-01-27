-- SPDX-License-Identifier: MIT
CREATE TABLE adm_mapping
(
   mapping_id varchar(240) not null, -- we accept 240 (3x80), see MappingIdValidation
   mapping_data text not null, -- no limit is JSON with dynamic length
   version integer,
   PRIMARY KEY (mapping_id)
);

CREATE TABLE scan_mapping
(
   mapping_id varchar(240) not null, -- we accept 240 (3x80), see MappingIdValidation
   mapping_data text not null, -- no limit is JSON with dynamic length
   version integer,
   PRIMARY KEY (mapping_id)
);

