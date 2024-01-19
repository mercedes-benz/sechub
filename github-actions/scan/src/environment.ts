// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import { GitHubInputData } from './input';

/**
 * Sets the necessary environment variables with the user input values.
 */
export function initEnvironmentVariables(data: GitHubInputData): void {
    shell.env['SECHUB_USERID'] = data.user;
    shell.env['SECHUB_APITOKEN'] = data.apiToken;
    shell.env['SECHUB_SERVER'] = data.url;
    shell.env['SECHUB_PROJECT'] = data.projectName;
    shell.env['SECHUB_DEBUG'] = data.debug;
    shell.env['SECHUB_TRUSTALL'] = data.trustAll;
}
