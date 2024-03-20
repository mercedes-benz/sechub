// SPDX-License-Identifier: MIT

import { failAction } from './action-helper';
import { scan } from './sechub-cli';
import { logExitCode } from './exitcode';
import { getFiles, getWorkspaceDir } from './fs-helper';
import { initReportFormats, initSecHubJson } from './init-scan';
import { collectReportData, reportOutputs, uploadArtifact } from './post-scan';
import { GitHubInputData, resolveGitHubInputData, INPUT_DATA_DEFAULTS } from './input';
import { initEnvironmentVariables } from './environment';
import { downloadClientRelease } from './client-download';
import { SecHubConfigurationModelBuilderData } from './configuration-builder';
import { ContentType, ScanType } from './configuration-model';

/**
 * Starts the launch process
 * @returns launch context
 */
export async function launch(): Promise<LaunchContext> {

    const context = createContext();

    init(context);

    executeScan(context);

    await postScan(context);

    return context;
}

export interface LaunchContext {
    jobUUID: string | undefined;
    debug: boolean;

    inputData: GitHubInputData;
    configFileLocation: string | null;

    /* json, html, spdx */
    reportFormats: string[];

    clientDownloadFolder: string;
    clientExecutablePath: string;

    lastClientExitCode: number;
    workspaceFolder: string;
    secHubJsonFilePath: string;

    secHubReportJsonObject: object | undefined;
    secHubReportJsonFileName: string;
}

export const LAUNCHER_CONTEXT_DEFAULTS: LaunchContext = {
    jobUUID: undefined,
    debug: false,

    inputData: INPUT_DATA_DEFAULTS,
    reportFormats: ['json'],
    clientDownloadFolder: '',
    configFileLocation: null,
    clientExecutablePath: '',

    lastClientExitCode: -1,

    secHubJsonFilePath: '',
    workspaceFolder: '',
    secHubReportJsonObject: undefined,
    secHubReportJsonFileName: '',
};


/**
 * Creates the initial launch context
 * @returns launch context
 */
function createContext(): LaunchContext {

    const gitHubInputData = resolveGitHubInputData();

    // client
    const clientVersion = gitHubInputData.sechubCLIVersion;

    if (clientVersion == null || clientVersion == '') {
        throw new Error('No SecHub client version defined!');
    }

    const expression = /\./gi;
    const clientVersionSubFolder = clientVersion.replace(expression, '_'); // avoid . inside path from user input
    const workspaceFolder = `${getWorkspaceDir()}`;
    const clientDownloadFolder = `${workspaceFolder}/.sechub-gha/client/${clientVersionSubFolder}`;
    const clientExecutablePath = `${clientDownloadFolder}/platform/linux-386/sechub`;

    const generatedSecHubJsonFilePath = `${workspaceFolder}/generated-sechub.json`;

    const builderData = createSafeBuilderData(gitHubInputData);

    const configFileLocation = initSecHubJson(generatedSecHubJsonFilePath, gitHubInputData.configPath, builderData);

    const reportFormats = initReportFormats(gitHubInputData.reportFormats);

    return {
        jobUUID: LAUNCHER_CONTEXT_DEFAULTS.jobUUID,
        secHubReportJsonObject: LAUNCHER_CONTEXT_DEFAULTS.secHubReportJsonObject,
        secHubReportJsonFileName: '',

        configFileLocation: configFileLocation,
        reportFormats: reportFormats,
        inputData: gitHubInputData,
        clientDownloadFolder: clientDownloadFolder,
        clientExecutablePath: clientExecutablePath,

        lastClientExitCode: LAUNCHER_CONTEXT_DEFAULTS.lastClientExitCode,

        secHubJsonFilePath: generatedSecHubJsonFilePath,
        workspaceFolder: workspaceFolder,

        debug: gitHubInputData.debug=='true',
    };
}

function createSafeBuilderData(gitHubInputData: GitHubInputData) {
    const builderData = new SecHubConfigurationModelBuilderData();

    builderData.includeFolders = gitHubInputData.includeFolders?.split(',');
    builderData.excludeFolders = gitHubInputData.excludeFolders?.split(',');

    builderData.scanTypes = ScanType.ensureAccepted(gitHubInputData.scanTypes?.split(','));
    builderData.contentType = ContentType.ensureAccepted(gitHubInputData.contentType);
    return builderData;
}

function init(context: LaunchContext) {

    initEnvironmentVariables(context.inputData);

    downloadClientRelease(context);
}

/**
 * Executes the scan.
 * @param configParameter Parameter for the sechub.json path. Can be null if the file was created by the action.
 * @param format Report format that should be downloaded
 */
function executeScan(context: LaunchContext) {
    scan(context);

    logExitCode(context.lastClientExitCode);
}

async function postScan(context: LaunchContext): Promise<void> {
    if (context.lastClientExitCode > 1) {
        // in this case this is an error and we cannot download the report - means fails always
        failAction(context.lastClientExitCode);
        return;
    }

    collectReportData(context);

    /* reporting - analysis etc. */
    reportOutputs(context.secHubReportJsonObject);

    /* upload artifact */
    await uploadArtifact(context, 'sechub scan-report', getFiles(`${context.workspaceFolder}/sechub_report_*.*`));

    if (context.lastClientExitCode !== 0 && context.inputData.failJobOnFindings === 'true') {
        failAction(context.lastClientExitCode);
    }
}


