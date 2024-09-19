-- SPDX-License-Identifier: MIT

-- In postgres we do a migration
-- But here, for h2, we do not migrate old data, because
-- h2 is only for testing!
-- Means no job migration done here.

DELETE FROM schedule_cipher_pool_data;