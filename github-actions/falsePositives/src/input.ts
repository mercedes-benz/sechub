// SPDX-License-Identifier: MIT

import * as core from '@actions/core';

export const configPath = core.getInput('config-path') || null;
export const url = core.getInput('url') || null;
export const apiToken = core.getInput('api-token') || null;
export const user = core.getInput('user') || null;
export const projectName = core.getInput('project-name') || null;
export const sechubCLIVersion = core.getInput('version');
export const debug = getDebug();
export const file = core.getInput('file') || null;
export const action = core.getInput('action', { required: true });

function getDebug(): boolean | null {
    const debug = core.getInput('debug')?.toLowerCase()?.trim() || null;
    if (debug === null) return null;
    return debug === 'true';
}
