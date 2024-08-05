-- SPDX-License-Identifier: MIT
ALTER TABLE scan_product_result
  ADD COLUMN meta_data text -- accept long text, also null accepted
;

