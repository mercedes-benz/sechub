// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import { LaunchContext } from './launcher';
import * as core from '@actions/core';
import * as shellCmdSanitizer from "./shell-cmd-sanitizer";

/**
 * Executes the scan method of the SecHub CLI. Sets the client exitcode inside context.
 * @param context launch context
 */
export function scan(context: LaunchContext) {
    const addScmHistory = context.inputData.addScmHistory === 'true' ? '-addScmHistory' : '';
    let shellCommand = `${context.clientExecutablePath} -configfile ${context.configFileLocation} -output ${context.workspaceFolder} ${addScmHistory} scan`;

    shellCommand = shellCmdSanitizer.sanitize(shellCommand);

    core.debug(`scan shell command: ${shellCommand}`);

    /* execute the scan */
    const shellString =  shell.exec(shellCommand);

    core.debug(`scan exit code: ${shellString.code}`);
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

            const jobUUID = extracted.trim();
            core.debug(`extractJobUUID: ${jobUUID}`);
            
            return jobUUID;
        }
    }
    core.debug('extractJobUUID: no job uuid found!');
    return '';
}

/**
 * Executes the getReport method of the SecHub CLI. Sets the client exitcode inside context.
 * @param jobUUID job UUID for which the report should be downloaded
 * @param format format in which the report should be downloaded
 * @param context launch context
*/
export function getReport(jobUUID: string, format: string, context: LaunchContext) {
    let shellCommand = `${context.clientExecutablePath} -jobUUID ${jobUUID} -project ${context.projectName} --reportformat ${format} getReport`;
    core.debug(`getReport shell command: ${shellCommand}`);

    shellCommand = shellCmdSanitizer.sanitize(shellCommand);
    
    const shellString =  shell.exec(shellCommand);
    
    core.debug(`get report exit code: ${shellString.code}`);
    context.lastClientExitCode= shellString.code;
}