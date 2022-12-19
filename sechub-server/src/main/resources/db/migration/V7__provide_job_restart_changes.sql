-- SPDX-License-Identifier: MIT
-- +++++++++++++++++++++++++++
--  Constraints ADM
-- +++++++++++++++++++++++++++
ALTER TABLE adm_job_information DROP CONSTRAINT c05_adm_job_information_jobuuid
-- jobUUId is no longer unique, because restarts can result in multiple job information

