-- SPDX-License-Identifier: MIT

-- we drop the old status entries
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.waiting';
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.running';