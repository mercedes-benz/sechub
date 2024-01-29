// SPDX-License-Identifier: MIT

import {failAction, handleError} from '../../shared/src/action-helper';
import {downloadRelease} from '../../shared/src/cli-helper';
import { scan } from '../../shared/src/sechub-cli';
import { logExitCode } from '../../shared/src/log-helper';
import { getFiles } from '../../shared/src/fs-helper';
import {initEnvironmentVariables, initReportFormats, initSecHubJson, ScanSettings} from './init-scan';
import { downloadReports, reportOutputs, uploadArtifact } from './post-scan';
import * as input from './input';
import * as settingsFile from './settings.json';

main().catch(handleError);

async function main(): Promise<void> {
    const scanSettings = initScan();
    const exitCode = executeScan(scanSettings.configParameter, scanSettings.reportFormats[0]);
    await postScan(scanSettings.reportFormats, exitCode);
}

/**
 * Initializes the scan and returns required scan settings.
 */
function initScan(): ScanSettings {
    initEnvironmentVariables();
    downloadRelease(input.sechubCLIVersion);

    const includeFolders = input.includeFolders?.split(',');
    const excludeFolders = input.excludeFolders?.split(',');
    const configParameter = initSecHubJson(input.configPath, includeFolders, excludeFolders);

    const reportFormats = initReportFormats(input.reportFormats);
    return {
        configParameter: configParameter,
        reportFormats: reportFormats,
    };
}

/**
 * Executes the scan.
 * @param configParameter Parameter for the sechub.json path. Can be null if the file was created by the action.
 * @param format Report format that should be downloaded
 */
export function executeScan(configParameter: string | null, format: string): number {
    const exitCode = scan(configParameter, format).code;
    logExitCode(exitCode);
    return exitCode;
}

/**
 * Executes several actions after the scan finished.
 * @param reportFormats formats in which the report should be downloaded
 * @param exitCode exit code from the scan
 */
export async function postScan(reportFormats: string[], exitCode: number): Promise<void> {
    const jsonReport = downloadReports(reportFormats.slice(1));
    reportOutputs(jsonReport);
    await uploadArtifact(settingsFile.artifactName, getFiles(settingsFile.filePattern));

    if (exitCode !== 0 && input.failJobOnFindings === 'true') {
        failAction(exitCode);
    }
}
