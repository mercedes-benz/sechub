-- update pds job
ALTER TABLE pds_job DROP COLUMN output_stream_text;

ALTER TABLE pds_job DROP COLUMN error_stream_text;

ALTER TABLE pds_job DROP COLUMN last_stream_text_refresh_request;

ALTER TABLE pds_job DROP COLUMN last_stream_text_update;
;
