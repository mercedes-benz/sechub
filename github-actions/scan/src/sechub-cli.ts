// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import { LaunchContext } from './launcher';
import * as core from '@actions/core';
/**
 * Executes the scan method of the SecHub CLI. Sets the client exitcode inside context.
 * @param parameter Parameters to execute the scan with
 * @param context: launch context
 */
export function scan(context: LaunchContext) {
    const shellString =  shell.exec(`${context.clientExecutablePath} -configfile ${context.configFileLocation} -output ${context.workspaceFolder} scan`);
    context.lastClientExitCode= shellString.code;

    if (context.lastClientExitCode!=0){
        core.error(shellString.stderr);
    }
    context.jobUUID=extractJobUUID(shellString.stdout);
}

export function extractJobUUID(output: string): string{
    const jobPrefix='job:';
    const index1 =output.indexOf(jobPrefix);
    if (index1>-1){
        const index2 = output.indexOf('\n', index1);
        if (index2>-1){
            const extracted=output.substring(index1+jobPrefix.length,index2);
            return extracted.trim();
        }
    }
    return '';
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

