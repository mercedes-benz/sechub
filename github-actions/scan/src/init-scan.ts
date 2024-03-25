// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as input from './input';
import * as core from '@actions/core';
import { createSecHubJsonFile } from '../../shared/src/cli-helper';
import { ReportFormat } from '../../shared/src/report-formats';

export interface ScanSettings {
    configPath: string | null;
    reportFormat: ReportFormat | null;
}

/**
 * Sets the necessary environment variables with the user input values.
 */
export function initEnvironmentVariables(): void {
    shell.env['SECHUB_USERID'] = input.user ?? (shell.env['SECHUB_USERID'] ?? '')
    shell.env['SECHUB_APITOKEN'] = input.apiToken ?? (shell.env['SECHUB_APITOKEN'] ?? '');
    shell.env['SECHUB_SERVER'] = input.url ?? (shell.env['SECHUB_SERVER'] ?? '');
    shell.env['SECHUB_PROJECT'] = input.projectName ?? (shell.env['SECHUB_PROJECT'] ?? '');
    shell.env['SECHUB_DEBUG'] = input.debug !== null ? input.debug.toString() : (shell.env['SECHUB_DEBUG'] ?? '');
}

/**
 * Returns the parameter to the sechub.json or creates it from the input parameters if configPath is not set.
 * @param configPath Path to the sechub.json
 * @param includeFolders list of folders to include to the scan
 * @param excludeFolders list of folders to exclude from the scan
 */
export function initSecHubJson(configPath: string | null, includeFolders: string[], excludeFolders: string[]): string | null {
    core.startGroup('Set config');
    if (!configPath) {
        createSecHubJsonFile(includeFolders, excludeFolders);
        return null;
    }

    core.info(`Config-Path was found: ${configPath}`);
    core.endGroup();
    return configPath;
}
