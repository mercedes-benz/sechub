-- SPDX-License-Identifier: MIT
ALTER TABLE scan_product_result
  ADD COLUMN project_id varchar(60) not null -- we accept 60 (3x20), see ProjectIdValidation
    DEFAULT '[undefined]' -- just for having a fallback here for upgraded with null, [,] are not valid user input so no clash with new projects
;
