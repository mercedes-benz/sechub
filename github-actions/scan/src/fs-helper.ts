// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as core from '@actions/core';
import * as path from 'path';

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
