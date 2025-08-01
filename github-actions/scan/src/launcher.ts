// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as fs from 'fs';
import { failAction, handleError } from './action-helper';
import { downloadClientRelease } from './client-download';
import { SecHubConfigurationModelBuilderData } from './configuration-builder';
import { initEnvironmentVariables } from './environment';
import { logExitCode } from './exitcode';
import { getFiles, getWorkspaceDir } from './fs-helper';
import { GitHubInputData, INPUT_DATA_DEFAULTS, resolveGitHubInputData } from './github-input';
import { initReportFormats, initSecHubJson } from './init-scan';
import { collectReportData, reportOutputs, uploadArtifact } from './post-scan';
import * as projectNameResolver from './projectname-resolver';
import { scan } from './sechub-cli';
import { defineFalsePositives } from './sechub-cli';
import { getPlatform, getPlatformDirectory } from './platform-helper';
import { safeAcceptedScanTypes, split } from './input-helper';
import { getClientVersion } from './client-version-helper';
import { ContentType } from './content-type';


/**
 * Starts the launch process
 * @returns launch context
 */
export async function launch(): Promise<LaunchContext> {

    const context = await createContext();

    await init(context);

    executeDefineFalsePositives(context);
    if (context.lastClientExitCode > 0) {
        // In case of an error during the defineFalsePositives step, we fail the action here!
        failAction(context.lastClientExitCode);
        return context;
    }

    await executeScan(context);

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

    secHubReportJsonObject: object | undefined;
    secHubReportJsonFileName: string;

    defineFalsePositivesFile: string;
}

export const LAUNCHER_CONTEXT_DEFAULTS: LaunchContext = {
    jobUUID: undefined,
    debug: false,

    projectName: '',

    inputData: INPUT_DATA_DEFAULTS,
    reportFormats: ['json'],
    clientVersion: '',
    clientDownloadFolder: '',
    configFileLocation: null,
    clientExecutablePath: '',

    lastClientExitCode: -1,

    workspaceFolder: '',
    secHubReportJsonObject: undefined,
    secHubReportJsonFileName: '',
    trafficLight: 'OFF',
    defineFalsePositivesFile: '',
};

function resolveClientDownloadFolder(clientVersion: string, gitHubInputData: GitHubInputData): string {

    if (clientVersion == 'build') {
        const buildDownloadFolder = gitHubInputData.clientBuildFolder + '/go';

        const isDirAndExists = fs.existsSync(buildDownloadFolder) && fs.lstatSync(buildDownloadFolder).isDirectory();
        if (!isDirAndExists) {
            handleError(`The client build folder path is not a directory or does not exist: ${buildDownloadFolder}`);
        }
        return buildDownloadFolder;
    }
    const expression = /\./gi;
    const clientVersionSubFolder = clientVersion.replace(expression, '_'); // avoid . inside path from user input
    return `${getWorkspaceDir()}/.sechub-gha/client/${clientVersionSubFolder}`;
}

/**
 * Creates the initial launch context
 * @returns launch context
 */
async function createContext(): Promise<LaunchContext> {
    const gitHubInputData = resolveGitHubInputData();
    const clientVersion = await getClientVersion(gitHubInputData.sechubCLIVersion);

    const workspaceFolder = getWorkspaceDir();
    const clientDownloadFolder = resolveClientDownloadFolder(clientVersion, gitHubInputData);
    let clientExecutablePath = `${clientDownloadFolder}/platform/${getPlatformDirectory()}/sechub`;
    if (getPlatform() === 'win32') {
        clientExecutablePath = clientExecutablePath.concat('.exe');
    }

    if (core.isDebug()) {
        core.debug('Client executable path set to:' + clientExecutablePath);
    }

    const generatedSecHubJsonFilePath = `${workspaceFolder}/generated-sechub.json`;

    const builderData = createSafeBuilderData(gitHubInputData);

    initSecHubJson(generatedSecHubJsonFilePath, gitHubInputData.configPath, builderData);

    const projectName = projectNameResolver.resolveProjectName(gitHubInputData, generatedSecHubJsonFilePath);

    const reportFormats = initReportFormats(gitHubInputData.reportFormats);

    return {
        jobUUID: LAUNCHER_CONTEXT_DEFAULTS.jobUUID,
        secHubReportJsonObject: LAUNCHER_CONTEXT_DEFAULTS.secHubReportJsonObject,
        secHubReportJsonFileName: '',

        configFileLocation: generatedSecHubJsonFilePath,
        reportFormats: reportFormats,
        inputData: gitHubInputData,
        clientVersion: clientVersion,
        clientDownloadFolder: clientDownloadFolder,
        clientExecutablePath: clientExecutablePath,

        projectName: projectName,

        lastClientExitCode: LAUNCHER_CONTEXT_DEFAULTS.lastClientExitCode,

        workspaceFolder: workspaceFolder,
        trafficLight: LAUNCHER_CONTEXT_DEFAULTS.trafficLight,
        debug: gitHubInputData.debug == 'true',
        defineFalsePositivesFile: gitHubInputData.defineFalsePositives,
    };
}

function createSafeBuilderData(gitHubInputData: GitHubInputData) {
    const builderData = new SecHubConfigurationModelBuilderData();

    builderData.includeFolders = split(gitHubInputData.includeFolders);
    builderData.excludeFolders = split(gitHubInputData.excludeFolders);

    builderData.scanTypes = safeAcceptedScanTypes(split(gitHubInputData.scanTypes));
    builderData.contentType = ContentType.safeAcceptedContentType(gitHubInputData.contentType);
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
async function executeScan(context: LaunchContext) {
    await scan(context);

    logExitCode(context.lastClientExitCode);
}

/**
 * Executes defineFalsePositive action of the SecHub GO client.
 * @param context launch context
 */
function executeDefineFalsePositives(context: LaunchContext) {
    defineFalsePositives(context);

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
