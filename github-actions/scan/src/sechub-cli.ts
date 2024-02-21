// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import { LaunchContext } from './launcher';
import * as core from '@actions/core';
/**
 * Executes the scan method of the SecHub CLI. Sets the client exitcode inside context.
 * @param parameter Parameters to execute the scan with
 * @param format Report format that should be fetched
 * @param context: launch context
 */
export function scan(format: string, context: LaunchContext) {
    const shellString =  shell.exec(`${context.clientExecutablePath} -configfile ${context.configFileLocation} -output ${context.workspaceFolder} -reportformat ${format} scan`);
    context.lastClientExitCode= shellString.code;

    if (context.lastClientExitCode!=0){
        core.error(shellString.stderr);
    }
}

/**
 * Executes the getReport method of the SecHub CLI. Sets the client exitcode inside context.
 * @param jobUUID job UUID for which the report should be downloaded
 * @param projectName name of the project for which the report should be downloaded
 * @param format format in which the report should be downloaded
 * @param context: launch context
 */
export function getReport(jobUUID: string, format: string, context: LaunchContext) {
    const shellString =  shell.exec(`${context.clientExecutablePath} -jobUUID ${jobUUID} -project ${context.inputData.projectName} --reportformat ${format} getReport`);
    context.lastClientExitCode= shellString.code;
}

