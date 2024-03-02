// SPDX-License-Identifier: MIT

import * as core from '@actions/core';

/**
 * Logs the exit code and uses error method if not 0.
 * @param code The given exit code
 */
export function logExitCode(code: number) {
    const message = `Exit code: ${code}`;
    if (code === 0) {
        core.info(message);
    } else {
        core.error(message);
    }
}
