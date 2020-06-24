CREATE TABLE pds_job
(
   uuid uuid not null,
   
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
