-- SPDX-License-Identifier: MIT
-- pds job represents job being scheduled by PDSBatchTriggerService
CREATE TABLE pds_job
(
   uuid uuid not null,
   
   server_id varchar(90) not null,  -- we accept 60 (3x30), see PDSServerIdentifierValidator
   
   state varchar(30) not null, -- enum value, max:30
   owner varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   
   created timestamp not null,
   started timestamp,
   ended timestamp,
   
   configuration varchar(8192) not null, -- we accept maximum of 8192 chars (8kb)
   
   result text, -- contains job result when done
   
   version integer,
   PRIMARY KEY (uuid)
);

-- pds hearbeat represents a heartbeat from a server with additional meta information
CREATE TABLE pds_heartbeat
(
   uuid uuid not null,
   
   updated timestamp,
   
   server_id varchar(90) not null,  -- we accept 60 (3x30), see PDSServerIdentifierValidator
   
   
   result text, -- contains last heartbeat information
   
   version integer,
   PRIMARY KEY (uuid)
);
