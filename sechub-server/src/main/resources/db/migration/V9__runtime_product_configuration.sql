CREATE TABLE scan_product_executor_config
(
   uuid uuid not null,
   name varchar(60) not null, -- we accept 60 (3 x 20) see ProductExecutorConfigValidation 
   executor_version integer,
   product_id varchar(30) not null, -- enum value, max:30
   setup text,
   version integer,

   project_id varchar(60) not null, -- we accept 60 (3x20), see ProjectIdValidation
   executed_by varchar(60) not null, -- we accept 60 (3 x 20) see UserIdValidation
   sechub_job_uuid uuid not null,
   config varchar(8192) not null, -- we accept maximum of 8192 chars (8kb)
   status varchar(30),
   started timestamp not null,
   ended timestamp,
   
   PRIMARY KEY (uuid)
)
;