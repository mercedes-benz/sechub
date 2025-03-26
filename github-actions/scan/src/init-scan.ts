// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import { SecHubConfigurationModelBuilderData, createSecHubConfigJsonString } from './configuration-builder';
import { getValidFormatsFromInput } from './report-formats';
import * as fs from 'fs';
import { addAdditonalExcludes } from './configuration-model-default-helper';

/**
 * Create a SecHub config JSON file at 'secHubJsonTargetFilePath'. If no custom config-path is defined, a config file will be 
 * generated from the input parameters.
 * 
 * @param secHubJsonTargetFilePath The target Sechub config file that will be created for the scan.
 * @param customSecHubConfigFilePath Path to the custom sechub.json (if defined)
 * @param builderData contains builder data which is used when no custom sechub configuration file is defined by user
 */
export function initSecHubJson(secHubJsonTargetFilePath: string, customSecHubConfigFilePath: string,  builderData: SecHubConfigurationModelBuilderData): void {
    core.startGroup('Set config');

    let jsonString = "";
    if (customSecHubConfigFilePath) {
        core.info(`Config-Path was found: ${customSecHubConfigFilePath}`);
        if (fs.existsSync(customSecHubConfigFilePath)) {
             core.debug(`Reading custom config file as json`);
             jsonString = fs.readFileSync(customSecHubConfigFilePath, 'utf8');
        } else {
            throw new Error(`Config-Path was defined, but no file exists at: ${customSecHubConfigFilePath}`);
        }
    } else {
        jsonString = createSecHubConfigJsonString(builderData);
    }
    /* additional post processing of defined/generated config file :*/
    core.debug(`Additional post processing of SecHub configuration model`);
    const jsonData: any = JSON.parse(jsonString);
    addAdditonalExcludes(jsonData);
    fs.writeFileSync(secHubJsonTargetFilePath, JSON.stringify(jsonData, null, 2));

    core.endGroup();
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
