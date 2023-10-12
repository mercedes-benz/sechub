// SPDX-License-Identifier: MIT

import * as core from '@actions/core';

/**
 * Logs the exit code and uses error method if not 0.
 * @param code The given exit code
 */
export function logExitCode(code: string) {
    const prefix = 'Exit code: ';
    if (code === '0') {
        core.info(prefix + code);
    } else {
        core.error(prefix + code);
    }
}
