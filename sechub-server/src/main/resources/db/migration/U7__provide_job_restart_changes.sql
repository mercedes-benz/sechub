-- SPDX-License-Identifier: MIT
-- +++++++++++++++++++++++++++
--  Constraints ADM
-- +++++++++++++++++++++++++++
ALTER TABLE adm_job_information ADD CONSTRAINT c05_adm_job_information_jobuuid unique (job_uuid);