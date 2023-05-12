-- SPDX-License-Identifier: MIT
CREATE TABLE schedule_sechub_job_data
(
   job_uuid uuid not null,
   
   id varchar(150) not null, -- we accept 150 (3x50) we use "id" ("key" is not possible because H2 keyword, so forbidden)
   val varchar(4096) not null, -- we accept 4kb here, we must use "val" instead of "value" ("value" is not possible because H2 keyword, so forbidden)
   
   created timestamp not null,
   
   version integer,
   PRIMARY KEY (job_uuid, id)
);

