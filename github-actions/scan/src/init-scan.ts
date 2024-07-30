// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import { SecHubConfigurationModelBuilderData, createSecHubConfigJsonFile as createSecHubConfigJsonFile } from './configuration-builder';
import { getValidFormatsFromInput } from './report-formats';
import * as fs from 'fs';

/**
 * Returns the path to the sechub.json. If no custom config-path is defined, a config file wille be
 * generated from the input parameters and this path will be returned.
 *
 * @param secHubJsonFilePath Path to the sechub.json
 * @param customSecHubConfigFilePath Path to the custom sechub.json (if defined)
 * @param builderData contains builder data which is used when no custom sechub configuration file is defined by user
 *
 * @returns resulting configuration file path
 */
export function initSecHubJson(secHubJsonFilePath: string, customSecHubConfigFilePath: string,  builderData: SecHubConfigurationModelBuilderData): string {
    core.startGroup('Set config');

    let configFilePath = customSecHubConfigFilePath;
    if (configFilePath) {
        if (fs.existsSync(configFilePath)) {
            core.info(`Config-Path was found: ${configFilePath}`);
        } else {
            throw new Error(`Config-Path was defined, but no file exists at: ${configFilePath}`);
        }

    } else {
        createSecHubConfigJsonFile(secHubJsonFilePath, builderData);
        configFilePath = secHubJsonFilePath;
    }
    core.endGroup();

    return configFilePath;
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
