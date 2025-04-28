// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import { GitHubInputData } from './github-input';
import * as core from '@actions/core';

/**
 * Sets the necessary environment variables with the user input values.
 */
export function initEnvironmentVariables(data: GitHubInputData, projectName: string): void {

    shell.env['SECHUB_USERID'] = getValueIfNotVariable(data.user);
    shell.env['SECHUB_SERVER'] = getValueIfNotVariable(data.url);
    shell.env['SECHUB_PROJECT'] = getValueIfNotVariable(projectName);

    shell.env['SECHUB_APITOKEN'] = data.apiToken;
    shell.env['SECHUB_DEBUG'] = data.debug;
    shell.env['SECHUB_TRUSTALL'] = data.trustAll;
}

function getValueIfNotVariable(valueOrEnvName: string): string | undefined {

    if (valueOrEnvName.startsWith('{{')) {
        return '';
    } else {
        return valueOrEnvName;
    }
}

