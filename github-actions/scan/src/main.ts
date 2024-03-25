// SPDX-License-Identifier: MIT

import { failAction, handleError } from '../../shared/src/action-helper';
import { setupSecHubCli } from '../../shared/src/cli-helper';
import { scan } from '../../shared/src/sechub-cli';
import { logExitCode } from '../../shared/src/log-helper';
import { getFiles } from '../../shared/src/fs-helper';
import { initEnvironmentVariables, initSecHubJson, ScanSettings } from './init-scan';
import { downloadJsonReport, reportOutputs, uploadArtifact } from './post-scan';
import * as input from './input';
import * as settingsFile from './settings.json';
import {checkReportFormat} from "../../shared/src/report-formats";
import * as core from '@actions/core';

main().catch(handleError);

async function main(): Promise<void> {
    const scanSettings = await initScan();
    const scanResult = executeScan(scanSettings);
    await postScan(scanSettings, scanResult);
}

/**
 * Initializes the scan and returns required scan settings.
 */
async function initScan(): Promise<ScanSettings> {
    initEnvironmentVariables();
    await setupSecHubCli(input.sechubCLIVersion);

    const includeFolders = input.includeFolders?.split(',') ?? [];
    const excludeFolders = input.excludeFolders?.split(',') ?? [];
    const configPath = initSecHubJson(input.configPath, includeFolders, excludeFolders);

    const reportFormat = checkReportFormat(input.reportFormat);
    return {
        configPath,
        reportFormat,
    };
}

type ScanResult = {
    exitCode: number
    jobUUID: string
}

const JOB_UUID_REGEX: RegExp = /Creating new SecHub job: ([a-f0-9-]+)/

/**
 * Executes the scan.
 * @param scanSettings The settings for the scan
 */
function executeScan(scanSettings: ScanSettings): ScanResult {
    const {stdout, code} = scan(scanSettings.configPath, scanSettings.reportFormat)
    const jobUUIDMatch = stdout.match(JOB_UUID_REGEX);
    const jobUUID = jobUUIDMatch === null ? '' : jobUUIDMatch[1];
    core.debug(`SecHub jobUUID: ${jobUUID}`);
    logExitCode(code);
    return { exitCode: code, jobUUID };
}

/**
 * Executes several actions after the scan finished.
 * @param scanSettings The settings for the scan
* @param scanResult The result of the scan
 */
async function postScan(scanSettings: ScanSettings, scanResult: ScanResult): Promise<void> {
    const jsonReport = downloadJsonReport(scanSettings, scanResult.jobUUID);
    reportOutputs(jsonReport);
    await uploadArtifact(settingsFile.artifactName, getFiles(settingsFile.filePattern));

    if (scanResult.exitCode !== 0 && input.failJobOnFindings) {
        failAction(scanResult.exitCode);
    }
}
