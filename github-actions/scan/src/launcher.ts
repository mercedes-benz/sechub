// SPDX-License-Identifier: MIT

import { failAction } from './action-helper';
import { scan } from './sechub-cli';
import { logExitCode } from './exitcode';
import { getFiles, getWorkspaceDir } from './fs-helper';
import { initReportFormats, initSecHubJson } from './init-scan';
import { downloadReports, reportOutputs, uploadArtifact } from './post-scan';
import { GitHubInputData, resolveGitHubInputData, INPUT_DATA_DEFAULTS } from './input';
import { initEnvironmentVariables } from './environment';
import { downloadClientRelease } from './client-download';
import { exit } from 'shelljs';



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
    inputData: GitHubInputData;
    configFileLocation: string | null;

    /* json, html, spdx */
    reportFormats: string[];

    clientDownloadFolder: string;
    clientExecutablePath: string;

    lastClientExitCode: number;
    workspaceFolder: string;
    secHubJsonFilePath: string;
}

export const LAUNCHER_CONTEXT_DEFAULTS: LaunchContext = {
    inputData: INPUT_DATA_DEFAULTS,
    reportFormats: ['json'],
    clientDownloadFolder: '',
    configFileLocation: null,
    clientExecutablePath: '',

    lastClientExitCode: -1, 

    secHubJsonFilePath: '',
    workspaceFolder: '',
};

/**
 * Create scan settings
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

    const includeFolders = gitHubInputData.includeFolders?.split(',');
    const excludeFolders = gitHubInputData.excludeFolders?.split(',');

    const generatedSecHubJsonFilePath = `${workspaceFolder}/sechub.json`;

    const configParameter = initSecHubJson(generatedSecHubJsonFilePath, gitHubInputData.configPath, includeFolders, excludeFolders);

    const reportFormats = initReportFormats(gitHubInputData.reportFormats);

    return {
        configFileLocation: configParameter,
        reportFormats: reportFormats,
        inputData: gitHubInputData,
        clientDownloadFolder: clientDownloadFolder,
        clientExecutablePath: clientExecutablePath,

        lastClientExitCode: LAUNCHER_CONTEXT_DEFAULTS.lastClientExitCode,

        secHubJsonFilePath: generatedSecHubJsonFilePath,
        workspaceFolder:workspaceFolder,
    };
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
    // TODO 2024-01-19: de-jcup: here only the first report format is used... is this not a bug?
    scan(context.reportFormats[0], context);

    logExitCode(context.lastClientExitCode);
}

async function postScan(context: LaunchContext): Promise<void> {
    if (context.lastClientExitCode > 1) {
        // in this case this is an error and we cannot download the report - means fails always
        failAction(context.lastClientExitCode);
        return;
    }
    const jsonReport = downloadReports(context, context.reportFormats.slice(1));

    /* reporting - analysis etc. */
    reportOutputs(jsonReport);

    /* upload artifact */
    await uploadArtifact(context, 'sechub scan-report', getFiles(`${context.workspaceFolder}/sechub_report_*.*`));

    if (context.lastClientExitCode !== 0 && context.inputData.failJobOnFindings === 'true') {
        failAction(context.lastClientExitCode);
    }
}


