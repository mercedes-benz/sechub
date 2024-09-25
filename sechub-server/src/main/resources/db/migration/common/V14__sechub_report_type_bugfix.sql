-- SPDX-License-Identifier: MIT
-- 0.27.0-server and 0.27.1-sesrver did store result enum values accidently as integers instead
-- using string representation - this was fixed already, but we must clean up wrong created entries 
-- here as well (new reports only, old ones where already migrated to 'RESULT' )
UPDATE scan_report SET result_type ='MODEL' WHERE result_type ='1';
