CREATE TABLE pds_schedule_job
(
   uuid uuid not null,
   created timestamp not null,
   ended timestamp,
   result varchar(30) not null, -- enum value, max:30
   state varchar(30) not null, -- enum value, max:30
   configuration varchar(8192) not null, -- we accept maximum of 8192 chars (8kb)
   owner varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   started timestamp,
   version integer,
   PRIMARY KEY (uuid)
);
