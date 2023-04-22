// SPDX-License-Identifier: MIT

import * as core from '@actions/core';

/**
 * Marks the action as failed with given exit code.
 * @param exitCode Exit code returned by the SecHub cli
 */
export function failAction(exitCode: number): void {
    core.setFailed(`Scan finished with exit code: ${exitCode}. Please check output.`);
}

/**
 * Logs the error and sets the action status as failed.
 * @param error Error message that should be logged
 */
export function handleError(error: string | Error): void {
    core.error(error);
    failAction(1);
}
