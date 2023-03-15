// SPDX-License-Identifier: MIT

import * as core from '@actions/core';

export const configPath = core.getInput('config-path');
export const url = core.getInput('url');
export const apiToken = core.getInput('api-token');
export const user = core.getInput('user');
export const projectName = core.getInput('project-name');
export const sechubCLIVersion = core.getInput('version');
export const debug = core.getInput('debug');
export const includeFolders = core.getInput('include-folders');
export const excludeFolders = core.getInput('exclude-folders');
export const reportFormats = core.getInput('report-formats');
export const failJobOnFindings = core.getInput('fail-job-with-findings');
