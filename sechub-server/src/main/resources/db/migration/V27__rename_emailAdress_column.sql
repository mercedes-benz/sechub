-- SPDX-License-Identifier: MIT
ALTER TABLE adm_user_selfregistration
  RENAME COLUMN email_adress TO email_address;
ALTER TABLE adm_user
  RENAME COLUMN user_email_adress TO user_email_address;

ALTER TABLE adm_user RENAME CONSTRAINT c01_adm_user_emailadress TO c01_adm_user_emailaddress;
