-- update pds job to have ouptut and error text columns
ALTER TABLE pds_job ADD COLUMN output_stream_txt text
          DEFAULT ''; -- avoid null values for old ob data

ALTER TABLE pds_job ADD COLUMN error_stream_txt text
          DEFAULT ''; -- avoid null values for old ob data
          
ALTER TABLE pds_job ADD COLUMN last_stream_txt_refresh_request timestamp; 
ALTER TABLE pds_job ADD COLUMN last_stream_txt_update timestamp;
;
