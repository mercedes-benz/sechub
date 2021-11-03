-- update pds job to have ouptut and error text columns
ALTER TABLE pds_job ADD COLUMN output_stream_text text
          DEFAULT ''; -- avoid null values for old ob data

ALTER TABLE pds_job ADD COLUMN error_stream_text text
          DEFAULT ''; -- avoid null values for old ob data
          
ALTER TABLE pds_job ADD COLUMN last_stream_text_refresh_request timestamp; 
ALTER TABLE pds_job ADD COLUMN last_stream_text_update timestamp;
;
