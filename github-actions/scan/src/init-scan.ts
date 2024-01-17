// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as input from './input';
import * as core from '@actions/core';
import {createSecHubJsonFile} from './cli-helper';
import { getValidFormatsFromInput } from './report-formats';

export interface ScanSettings {
    configParameter: string | null;
    reportFormats: string[];
}

/**
 * Sets the necessary environment variables with the user input values.
 */
export function initEnvironmentVariables(): void {
    shell.env['SECHUB_USERID'] = input.user;
    shell.env['SECHUB_APITOKEN'] = input.apiToken;
    shell.env['SECHUB_SERVER'] = input.url;
    shell.env['SECHUB_PROJECT'] = input.projectName;
    shell.env['SECHUB_DEBUG'] = input.debug;
}

/**
 * Returns the parameter to the sechub.json or creates it from the input parameters if configPath is not set.
 * @param configPath Path to the sechub.json
 * @param includeFolders list of folders to include to the scan
 * @param excludeFolders list of folders to exclude from the scan
 */
export function initSecHubJson(configPath: string, includeFolders: string[], excludeFolders: string[]): string | null {
    core.startGroup('Set config');
    if (!configPath) {
        createSecHubJsonFile(includeFolders, excludeFolders);
        return null;
    }

    core.info(`Config-Path was found: ${configPath}`);
    const configParameter = `-configfile '${configPath}'`;
    core.endGroup();
    return configParameter;
}

/**
 * Initializes the report formats and ensures there is at least one valid report format selected.
 * @param reportFormats formats in which the report should be downloaded
 */
export function initReportFormats(reportFormats: string): string[] {
    const formats = getValidFormatsFromInput(reportFormats);
    if (formats.length === 0) {
        throw new Error('No valid report formats selected!');
    }

    ensureJsonReportAtBeginning(formats);

    return formats;
}

/**
 * Adds missing json format at the beginning or moves it to the first position.
 * The scan will use the first report format and to download other report formats it's required to get the job uuid from the json report.
 * @param reportFormats the selected report formats
 */
function ensureJsonReportAtBeginning(reportFormats: string[]): void {
    if (!reportFormats.includes('json')) {
        reportFormats.unshift('json');
    }

    if (reportFormats[0] !== 'json') {
        const index = reportFormats.findIndex((item) => item === 'json');
        reportFormats.splice(index, 1);
        reportFormats.unshift('json');
    }
}
