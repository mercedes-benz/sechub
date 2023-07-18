-- SPDX-License-Identifier: MIT
-- remove former added column "messages"
ALTER TABLE scan_product_result
   DROP COLUMN messages text;