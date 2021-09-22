-- update pds job
ALTER TABLE pds_job DROP COLUMN output_stream_txt;

ALTER TABLE pds_job DROP COLUMN error_stream_txt;

ALTER TABLE pds_job DROP COLUMN last_stream_txt_refresh_request;

ALTER TABLE pds_job DROP COLUMN last_stream_txt_update;
;
