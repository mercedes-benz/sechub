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
    return shell.env['GITHUB_WORKSPACE'] || '';
}

/**
 * Get parent folder of workspace directory.
 */
export function getWorkspaceParentDir() {
    return path.dirname(getWorkspaceDir());
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

    const shellExecResult = shell.exec(command);

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
    return child.exec(command);
}