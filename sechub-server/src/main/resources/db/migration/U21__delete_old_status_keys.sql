-- SPDX-License-Identifier: MIT

-- we drop the new introduced status entries
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.initializing';
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.ready_to_start';
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.started';
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.canceled';
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.cancel_requested';
DELETE FROM adm_status WHERE status_id='status.scheduler.jobs.ended';
