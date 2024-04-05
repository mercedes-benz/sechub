// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as core from '@actions/core';
import * as path from 'path';
import * as child from 'child_process';

import { ShellString } from 'shelljs';

/**
 * Get workspace directory.
 */
export function getWorkspaceDir(): string {
    /* github workspace is something like: 
    * /home/runner/work/sechub/
     * means we are at root level when the action is used
     * from outside
     * Means: we have a /home/runner/work/sechub/runtime folder
     *                  /home/runner/work/other-repo/runtime
     *  
     * For local builds/runs this must be done as well.
     * 
     */
    const workspace=shell.env['GITHUB_WORKSPACE'];
    if (workspace==null){
        /* not set, means local,we are inside github-actions/scan */
        return '../../';
    }else{
        return `${workspace}`;
    }
    
}

/**
 * Get parent folder of workspace directory.
 */
export function ensuredWorkspaceFolder() {
    const ensuredWorkspaceFolder= path.dirname(getWorkspaceDir());
    return ensuredWorkspaceFolder;
}

/**
 * Get all files in current directory for given pattern.
 * @param pattern Used to filter files
 */
export function getFiles(pattern: string): string[] {
    const reportFiles: string[] = [];

    shell.ls(pattern).forEach(function (file) {
        core.debug('file: ' + file);
        reportFiles.push(`${getWorkspaceDir()}/${file}`);
    });

    return reportFiles;
}

export class ShellFailedWithExitCodeNotAcceptedError extends Error {
    constructor(command: string, shellExecResult: ShellString, acceptedExitCodes: number[]) {
        super(`Shell script call failed.\nExit code: ${shellExecResult.code} - accepted would be: ${acceptedExitCodes}.\nCommand: "${command}"\nStdErr: ${shellExecResult.stderr}\nStdOut: ${shellExecResult.stdout}`);
    }
}

/**
 * Executes given command by shell synchronous - errors are handled.
 * Attention: This mechanism has problems with script execution where child processes are created!
 * In this case the script execution by shelljs freezes! Workaround here: Use shellExecAsync(..) in 
 * this case!
 * @param command command to execute
 * @param acceptedExitCodes - an array with accepted exit codes. if not defined only 0 is accepted
 * @throws ShellFailedWithExitCodeNotZeroError
 * @returns shellstring 
 */
export function shellExecSynchOrFail(command: string, acceptedExitCodes: number[] = [0]): ShellString {
    core.debug(`shellExecSynchOrFail: ${command}, acceptedExitCodes: ${acceptedExitCodes}`);

    const shellExecResult = shell.exec(command);
    core.debug(`shellExecSynchOrFail: exitCode: ${shellExecResult.code}`);

    if (! acceptedExitCodes.includes(shellExecResult.code)){
        throw new ShellFailedWithExitCodeNotAcceptedError(command, shellExecResult, acceptedExitCodes);
    }
    return shellExecResult;
}

/**
 * Executes given command asynchronous
 * @param command command to execute
 * @returns child process
 */
export function shellExecAsync(command: string): child.ChildProcess {
    core.debug(`shellExecAsync: ${command}`);
    return child.exec(command);
}