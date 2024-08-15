-- SPDX-License-Identifier: MIT
-- SecHub report has more fields. This data is stored inside ScanResult entity.
-- formerly the result column did contain JSON with SecHub result inside
-- because result has not the fields wanted, we store now the complete report model inside.
-- For backward compatibillity we introduce a new column scan_result_type to identify
-- old and new variants. The default for the migration will be "RESULT" which represents
-- a simple "SecHubResult". "MODEL" instead will be used by the new approach and contains
-- a full report model inside.

ALTER TABLE scan_report 
   ADD COLUMN result_type varchar(24) -- we accept 24 (3x8), see ScanReportResultType
          DEFAULT 'RESULT' -- set default, see ScanReportResultType#RESULT , so old data is "RESULT"
;
