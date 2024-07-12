-- remove old unencrypted config column
ALTER TABLE schedule_sechub_job DROP COLUMN unencrypted_configuration;