// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as core from '@actions/core';
import * as path from 'path';
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

export class ShellFailedWithExitCodeNotZeroError extends Error {
    constructor(command: string, shellExecResult: ShellString) {
        super(`Shell script call failed.\nExit code: ${shellExecResult.code}\nCommand: "${command}"\nStdErr: ${shellExecResult.stderr}`);
    }
}

/**
 * Executes given command by shell - errors are handled
 * @param command 
 * @throws ShellFailedWithExitCodeNotZeroError
 * @returns shellstring 
 */
export function shellExecOrFail(command: string): ShellString {
    const shellExecResult = shell.exec(command);

    if ( shellExecResult.code!=0){
        throw new ShellFailedWithExitCodeNotZeroError(command, shellExecResult);
    }
    return shellExecResult;
}