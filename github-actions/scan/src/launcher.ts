// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import { failAction } from './action-helper';
import { downloadClientRelease } from './client-download';
import { SecHubConfigurationModelBuilderData } from './configuration-builder';
import { ContentType, ScanType } from './configuration-model';
import { initEnvironmentVariables } from './environment';
import { logExitCode } from './exitcode';
import { getFiles, getWorkspaceDir } from './fs-helper';
import { GitHubInputData, INPUT_DATA_DEFAULTS, resolveGitHubInputData } from './github-input';
import { initReportFormats, initSecHubJson } from './init-scan';
import { collectReportData, reportOutputs, uploadArtifact } from './post-scan';
import * as projectNameResolver from './projectname-resolver';
import { scan } from './sechub-cli';
import { getPlatform, getPlatformDirectory } from './platform-helper';
import { split } from './input-helper';
import { getClientVersion } from './client-version-helper';

/**
 * Starts the launch process
 * @returns launch context
 */
export async function launch(): Promise<LaunchContext> {

    const context = await createContext();

    await init(context);

    executeScan(context);

    await postScan(context);

    return context;
}

export interface LaunchContext {
    trafficLight: string;
    jobUUID: string | undefined;

    projectName: string;

    debug: boolean;

    inputData: GitHubInputData;
    configFileLocation: string | null;

    /* json, html, spdx */
    reportFormats: string[];

    clientVersion: string;
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

    projectName: '',

    inputData: INPUT_DATA_DEFAULTS,
    reportFormats: ['json'],
    clientVersion: '1.6.1',
    clientDownloadFolder: '',
    configFileLocation: null,
    clientExecutablePath: '',

    lastClientExitCode: -1,

    secHubJsonFilePath: '',
    workspaceFolder: '',
    secHubReportJsonObject: undefined,
    secHubReportJsonFileName: '',
    trafficLight: 'OFF'
};


/**
 * Creates the initial launch context
 * @returns launch context
 */
async function createContext(): Promise<LaunchContext> {

    const gitHubInputData = resolveGitHubInputData();

    const clientVersion = await getClientVersion(gitHubInputData.sechubCLIVersion);
    const expression = /\./gi;
    const clientVersionSubFolder = clientVersion.replace(expression, '_'); // avoid . inside path from user input
    const workspaceFolder = getWorkspaceDir();
    const clientDownloadFolder = `${workspaceFolder}/.sechub-gha/client/${clientVersionSubFolder}`;
    let clientExecutablePath = `${clientDownloadFolder}/platform/${getPlatformDirectory()}/sechub`;
    if (getPlatform() === 'win32') {
        clientExecutablePath = clientExecutablePath.concat('.exe');
    }

    const generatedSecHubJsonFilePath = `${workspaceFolder}/generated-sechub.json`;

    const builderData = createSafeBuilderData(gitHubInputData);

    const configFileLocation = initSecHubJson(generatedSecHubJsonFilePath, gitHubInputData.configPath, builderData);

    const projectName = projectNameResolver.resolveProjectName(gitHubInputData, configFileLocation);

    const reportFormats = initReportFormats(gitHubInputData.reportFormats);

    return {
        jobUUID: LAUNCHER_CONTEXT_DEFAULTS.jobUUID,
        secHubReportJsonObject: LAUNCHER_CONTEXT_DEFAULTS.secHubReportJsonObject,
        secHubReportJsonFileName: '',

        configFileLocation: configFileLocation,
        reportFormats: reportFormats,
        inputData: gitHubInputData,
        clientVersion: clientVersion,
        clientDownloadFolder: clientDownloadFolder,
        clientExecutablePath: clientExecutablePath,

        projectName: projectName,

        lastClientExitCode: LAUNCHER_CONTEXT_DEFAULTS.lastClientExitCode,

        secHubJsonFilePath: generatedSecHubJsonFilePath,
        workspaceFolder: workspaceFolder,
        trafficLight: LAUNCHER_CONTEXT_DEFAULTS.trafficLight,
        debug: gitHubInputData.debug == 'true'
    };
}

function createSafeBuilderData(gitHubInputData: GitHubInputData) {
    const builderData = new SecHubConfigurationModelBuilderData();

    builderData.includeFolders = split(gitHubInputData.includeFolders);
    builderData.excludeFolders = split(gitHubInputData.excludeFolders);

    builderData.scanTypes = ScanType.ensureAccepted(split(gitHubInputData.scanTypes));
    builderData.contentType = ContentType.ensureAccepted(gitHubInputData.contentType);
    return builderData;
}

async function init(context: LaunchContext) {
    core.debug(`Init for project : ${context.projectName}`);
    initEnvironmentVariables(context.inputData, context.projectName);

    await downloadClientRelease(context);
}

/**
 * Executes the scan.
 * @param context launch context
 */
function executeScan(context: LaunchContext) {
    scan(context);

    logExitCode(context.lastClientExitCode);
}

async function postScan(context: LaunchContext): Promise<void> {
    core.debug(`postScan(1): context.lastExitCode=${context.lastClientExitCode}`);

    if (context.lastClientExitCode > 1) {
        // in this case (not 0, not 1) this is an error and we cannot download the report - here we fail always!
        failAction(context.lastClientExitCode);
        return;
    }

    collectReportData(context);

    /* reporting - analysis etc. */
    context.trafficLight = reportOutputs(context.secHubReportJsonObject);

    /* upload artifacts */
    await uploadArtifact(context, 'sechub scan-report', getFiles(`${context.workspaceFolder}/sechub_report_*.*`));

    core.debug(`postScan(2): context.lastExitCode=${context.lastClientExitCode}, context.trafficLight='${context.trafficLight}', context.inputData.failJobOnFindings='${context.inputData.failJobOnFindings}'`);
    if (context.trafficLight == 'RED' || context.trafficLight == 'OFF') {
        if (context.inputData.failJobOnFindings == 'true' || context.inputData.failJobOnFindings == '') {
            failAction(1);
        }
    }
}
