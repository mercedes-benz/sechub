-- SPDX-License-Identifier: MIT
ALTER TABLE adm_user_selfregistration
  RENAME COLUMN email_address TO email_adress;
ALTER TABLE adm_user
  RENAME COLUMN user_email_address TO user_email_adress;

ALTER TABLE adm_user RENAME CONSTRAINT c01_adm_user_emailaddress TO c01_adm_user_emailaddress;
