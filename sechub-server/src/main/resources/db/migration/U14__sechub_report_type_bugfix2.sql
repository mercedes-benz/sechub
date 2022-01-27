-- SPDX-License-Identifier: MIT
-- Undo / revert string enum to (wrong) integer representation
UPDATE scan_report SET result_type ='1' WHERE result_type ='MODEL';
